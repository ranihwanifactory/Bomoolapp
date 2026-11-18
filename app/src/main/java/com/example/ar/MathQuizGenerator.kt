package com.example.ar

import com.example.model.TreasureRarity
import kotlin.random.Random

data class MathQuizProblem(
    val expression: String,
    val correctAnswer: Int,
    val choices: List<Int>,
    val hint: String,
    val difficultyLabel: String
)

object MathQuizGenerator {

    fun generate(
        playerLevel: Long,
        rarity: TreasureRarity,
        random: Random = Random.Default
    ): MathQuizProblem {
        val tier = when {
            rarity.sortOrder >= 3 || playerLevel >= 9L -> 3
            rarity.sortOrder == 2 || playerLevel >= 4L -> 2
            else -> 1
        }

        val raw = when (tier) {
            1 -> generateTier1(random)
            2 -> generateTier2(random)
            else -> generateTier3(random)
        }

        val choices = buildChoices(raw.correctAnswer, random)
        return raw.copy(choices = choices)
    }

    private fun generateTier1(random: Random): MathQuizProblem {
        val label = "초등 기초"
        return when (random.nextInt(2)) {
            0 -> {
                // Easy 1- or 2-digit addition without overwhelming carry
                val a = random.nextInt(4, 29)
                val b = random.nextInt(3, 19)
                val ans = a + b
                MathQuizProblem(
                    expression = "$a + $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "$a 에 $b 만큼 차례대로 더해 보세요!",
                    difficultyLabel = label
                )
            }
            else -> {
                // Positive subtraction
                val b = random.nextInt(3, 16)
                val ans = random.nextInt(4, 24)
                val a = ans + b
                MathQuizProblem(
                    expression = "$a - $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "$a 에서 $b 을 거꾸로 세어 빼 보세요!",
                    difficultyLabel = label
                )
            }
        }
    }

    private fun generateTier2(random: Random): MathQuizProblem {
        val label = "초등 도전"
        return when (random.nextInt(4)) {
            0 -> {
                // Times table (구구단)
                val a = random.nextInt(2, 10)
                val b = random.nextInt(2, 10)
                val ans = a * b
                MathQuizProblem(
                    expression = "$a × $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "${a}단을 떠올려 볼까요? ${a}를 ${b}번 더해요!",
                    difficultyLabel = label
                )
            }
            1 -> {
                // Exact division
                val divisor = random.nextInt(2, 10)
                val ans = random.nextInt(2, 10)
                val dividend = divisor * ans
                MathQuizProblem(
                    expression = "$dividend ÷ $divisor = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "$divisor 에 어떤 수를 곱하면 $dividend 이 될까요?",
                    difficultyLabel = label
                )
            }
            2 -> {
                // Two-digit addition
                val a = random.nextInt(18, 65)
                val b = random.nextInt(14, 39)
                val ans = a + b
                MathQuizProblem(
                    expression = "$a + $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "1의 자리끼리 더하고 10의 자리도 더해 보세요!",
                    difficultyLabel = label
                )
            }
            else -> {
                // Two-digit subtraction
                val b = random.nextInt(12, 38)
                val ans = random.nextInt(11, 45)
                val a = ans + b
                MathQuizProblem(
                    expression = "$a - $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "1의 자리부터 차근차근 빼 보세요!",
                    difficultyLabel = label
                )
            }
        }
    }

    private fun generateTier3(random: Random): MathQuizProblem {
        val label = "초등 고수"
        return when (random.nextInt(4)) {
            0 -> {
                // Parentheses mixed calculation: (a + b) × c
                val a = random.nextInt(2, 7)
                val b = random.nextInt(2, 7)
                val c = random.nextInt(2, 6)
                val ans = (a + b) * c
                MathQuizProblem(
                    expression = "($a + $b) × $c = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "괄호 안의 ($a + $b)를 먼저 계산한 뒤 $c 를 곱해요!",
                    difficultyLabel = label
                )
            }
            1 -> {
                // Order of operations: a × b + c
                val a = random.nextInt(3, 9)
                val b = random.nextInt(3, 9)
                val c = random.nextInt(4, 15)
                val ans = a * b + c
                MathQuizProblem(
                    expression = "$a × $b + $c = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "곱셈 ($a × $b)을 먼저 계산하고 $c 를 더해요!",
                    difficultyLabel = label
                )
            }
            2 -> {
                // Two-digit × one-digit
                val a = random.nextInt(11, 20)
                val b = random.nextInt(2, 6)
                val ans = a * b
                MathQuizProblem(
                    expression = "$a × $b = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "(10 × $b)와 (${a - 10} × $b)를 나누어 더해 보세요!",
                    difficultyLabel = label
                )
            }
            else -> {
                // Slightly larger exact division
                val divisor = random.nextInt(3, 9)
                val ans = random.nextInt(6, 16)
                val dividend = divisor * ans
                MathQuizProblem(
                    expression = "$dividend ÷ $divisor = ?",
                    correctAnswer = ans,
                    choices = emptyList(),
                    hint = "$divisor × ? = $dividend 이 되는 수를 찾아보세요!",
                    difficultyLabel = label
                )
            }
        }
    }

    internal fun buildChoices(correct: Int, random: Random = Random.Default): List<Int> {
        val candidates = listOf(-10, -4, -3, -2, -1, 1, 2, 3, 4, 10)
            .map { correct + it }
            .filter { it > 0 && it != correct }
            .shuffled(random)

        val picked = linkedSetOf(correct)
        for (c in candidates) {
            if (picked.size >= 4) break
            picked.add(c)
        }
        var extra = 1
        while (picked.size < 4) {
            val next = correct + extra
            if (next > 0) picked.add(next)
            extra++
        }
        return picked.toList().shuffled(random)
    }
}
