package com.example.saulifeapp.utils

object TextParser {

    fun extractMedicineCandidates(rawText: String): List<String> {
        return rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { cleanLine(it) }
            .filter { it.length > 2 }
            .filterNot { it.all { ch -> ch.isDigit() } }
            .distinct()
    }

    private fun cleanLine(line: String): String {
        return line
            .replace(Regex("[^\\p{L}\\p{N}\\s\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s\\-]"), "")
            .replace(Regex("\\s+"), " ")
    }

    fun extractCoreName(text: String): String {
        return normalize(text)
            .replace(Regex("\\b\\d+\\s?(mg|мг|ml|мл|g|гр)?\\b"), "")
            .replace(Regex("\\b(tab|caps|капсулы|таблетки|ампулы|solution|sirup|syrup)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}