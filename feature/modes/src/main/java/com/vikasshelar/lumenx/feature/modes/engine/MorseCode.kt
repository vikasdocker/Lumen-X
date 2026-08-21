package com.vikasshelar.lumenx.feature.modes.engine

object MorseCode {
    private val codes = mapOf(
        'A' to ".-",
        'B' to "-...",
        'C' to "-.-.",
        'D' to "-..",
        'E' to ".",
        'F' to "..-.",
        'G' to "--.",
        'H' to "....",
        'I' to "..",
        'J' to ".---",
        'K' to "-.-",
        'L' to ".-..",
        'M' to "--",
        'N' to "-.",
        'O' to "---",
        'P' to ".--.",
        'Q' to "--.-",
        'R' to ".-.",
        'S' to "...",
        'T' to "-",
        'U' to "..-",
        'V' to "...-",
        'W' to ".--",
        'X' to "-..-",
        'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----",
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----.",
    )

    fun encode(text: String): List<String> {
        return normalizedWords(text).flatMapIndexed { wordIndex, word ->
            val encodedWord = word.mapNotNull { codes[it] }
            if (wordIndex == 0) encodedWord else listOf(WordGapToken) + encodedWord
        }
    }

    fun patternFor(
        text: String,
        unitMillis: Long = FlashModeSafetyLimits.DefaultMorseUnitMillis,
        repeats: Boolean = true,
        includeCycleGap: Boolean = true,
    ): FlashModePattern {
        val unit = unitMillis.coerceIn(
            FlashModeSafetyLimits.MinMorseUnitMillis,
            FlashModeSafetyLimits.MaxMorseUnitMillis,
        )
        val steps = mutableListOf<FlashPatternStep>()
        val words = normalizedWords(text)

        words.forEachIndexed { wordIndex, word ->
            if (wordIndex > 0 && steps.isNotEmpty()) {
                steps += FlashPatternStep.exact(enabled = false, durationMillis = unit * WordGapUnits)
            }

            var encodedLettersInWord = 0
            word.forEach { character ->
                val code = codes[character] ?: return@forEach
                if (encodedLettersInWord > 0) {
                    steps += FlashPatternStep.exact(enabled = false, durationMillis = unit * LetterGapUnits)
                }
                code.forEachIndexed { symbolIndex, symbol ->
                    if (symbolIndex > 0) {
                        steps += FlashPatternStep.exact(enabled = false, durationMillis = unit * SymbolGapUnits)
                    }
                    steps += FlashPatternStep.exact(
                        enabled = true,
                        durationMillis = unit * if (symbol == '-') DashUnits else DotUnits,
                    )
                }
                encodedLettersInWord++
            }
        }

        if (includeCycleGap && steps.isNotEmpty()) {
            steps += FlashPatternStep.exact(enabled = false, durationMillis = unit * WordGapUnits)
        }

        return FlashModePattern(
            steps = steps,
            repeats = repeats,
        )
    }

    private fun normalizedWords(text: String): List<String> {
        return text
            .uppercase()
            .take(FlashModeSafetyLimits.MaxMorseCharacters)
            .split(Regex("\\s+"))
            .map { word -> word.filter { it in codes } }
            .filter { it.isNotEmpty() }
    }

    private const val DotUnits = 1L
    private const val DashUnits = 3L
    private const val SymbolGapUnits = 1L
    private const val LetterGapUnits = 3L
    private const val WordGapUnits = 7L
    private const val WordGapToken = "/"
}
