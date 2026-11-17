package com.example.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.asin

data class DeviceOrientation(
    val azimuthDeg: Float = 0f, // 0..360
    val pitchDeg: Float = 0f,   // -90..90
    val hasHardwareSensor: Boolean = false
)

class ArSensorController(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    private val _orientation = MutableStateFlow(
        DeviceOrientation(hasHardwareSensor = rotationSensor != null)
    )
    val orientation: StateFlow<DeviceOrientation> = _orientation.asStateFlow()

    private var manualPanAzimuth = 0f
    private var manualPanPitch = 0f
    private var sensorAzimuth = 0f
    private var sensorPitch = 0f
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var isListening = false

    fun start() {
        if (isListening) return
        rotationSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            isListening = true
        }
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    fun addManualPan(deltaAzimuthDeg: Float, deltaPitchDeg: Float) {
        manualPanAzimuth = normalizeAzimuth(manualPanAzimuth + deltaAzimuthDeg)
        manualPanPitch = (manualPanPitch + deltaPitchDeg).coerceIn(-45f, 45f)
        emitCombined()
    }

    fun resetPan() {
        manualPanAzimuth = 0f
        manualPanPitch = 0f
        emitCombined()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            // Remap for portrait screen held upright facing forward
            val remapped = FloatArray(9)
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remapped
            )
            SensorManager.getOrientation(remapped, orientationAngles)
            val rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val rawPitch = -Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

            // Smooth angular wrap-around
            val targetAz = normalizeAzimuth(rawAzimuth)
            val diff = shortestAngleDiff(sensorAzimuth, targetAz)
            sensorAzimuth = normalizeAzimuth(sensorAzimuth + diff * 0.25f)
            sensorPitch = (sensorPitch + (rawPitch.coerceIn(-65f, 65f) - sensorPitch) * 0.25f)
            emitCombined()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun emitCombined() {
        _orientation.value = DeviceOrientation(
            azimuthDeg = normalizeAzimuth(sensorAzimuth + manualPanAzimuth),
            pitchDeg = (sensorPitch + manualPanPitch).coerceIn(-65f, 65f),
            hasHardwareSensor = rotationSensor != null
        )
    }

    companion object {
        fun normalizeAzimuth(deg: Float): Float {
            var res = deg % 360f
            if (res < 0f) res += 360f
            return res
        }

        fun shortestAngleDiff(fromDeg: Float, toDeg: Float): Float {
            var diff = (toDeg - fromDeg + 540f) % 360f - 180f
            if (diff < -180f) diff += 360f
            return diff
        }
    }
}
