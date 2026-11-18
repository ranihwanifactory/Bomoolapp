package com.example.ar

import com.example.model.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathQuizGeneratorTest {

    @Test
    fun generate_acrossAllTiers_alwaysHasFourUniquePositiveChoicesContainingAnswer() {
        val random = Random(42)
        val levels = listOf(1L, 3L, 5L, 9L, 15L)
        for (level in levels) {
            for (rarity in TreasureRarity.entries) {
                repeat(20) {
                    val quiz = MathQuizGenerator.generate(level, rarity, random)
                    assertEquals(4, quiz.choices.size)
                    assertEquals(4, quiz.choices.toSet().size)
                    assertTrue("All choices must be positive", quiz.choices.all { it > 0 })
                    assertTrue("Choices must contain correct answer", quiz.correctAnswer in quiz.choices)
                    assertTrue("Expression must end with '= ?'", quiz.expression.endsWith("= ?"))
                    assertTrue("Hint must not be blank", quiz.hint.isNotBlank())
                }
            }
        }
    }

    @Test
    fun generate_evaluatesExpressionCorrectly() {
        val random = Random(1234)
        repeat(100) {
            val level = listOf(1L, 5L, 12L).random(random)
            val rarity = TreasureRarity.entries.random(random)
            val quiz = MathQuizGenerator.generate(level, rarity, random)
            val evaluated = evaluateSimpleKoreanMath(quiz.expression)
            assertEquals("Mismatch for ${quiz.expression}", evaluated, quiz.correctAnswer)
        }
    }

    private fun evaluateSimpleKoreanMath(expr: String): Int {
        val body = expr.removeSuffix("= ?").trim()
        // Case 1: (a + b) × c
        if (body.startsWith("(")) {
            val closeIdx = body.indexOf(')')
            val inner = body.substring(1, closeIdx).trim()
            val innerParts = inner.split(" + ")
            val sum = innerParts[0].toInt() + innerParts[1].toInt()
            val factor = body.substring(closeIdx + 1).trim().removePrefix("×").trim().toInt()
            return sum * factor
        }
        // Case 2: a × b + c
        if (" × " in body && " + " in body) {
            val plusParts = body.split(" + ")
            val mulParts = plusParts[0].split(" × ")
            return mulParts[0].toInt() * mulParts[1].toInt() + plusParts[1].toInt()
        }
        // Binary operations
        return when {
            " + " in body -> body.split(" + ").let { it[0].toInt() + it[1].toInt() }
            " - " in body -> body.split(" - ").let { it[0].toInt() - it[1].toInt() }
            " × " in body -> body.split(" × ").let { it[0].toInt() * it[1].toInt() }
            " ÷ " in body -> body.split(" ÷ ").let { it[0].toInt() / it[1].toInt() }
            else -> error("Unsupported expr: $expr")
        }
    }
}
