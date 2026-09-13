package com.example.itantra.tts

class Tokenizer {

    fun tokenizeEnglish(text: String): LongArray {

        val vocabulary = mutableMapOf<Char, Long>()

        vocabulary[' '] = 13L

        for (c in 'a'..'z') {
            vocabulary[c] = 14L + (c - 'a')
        }

        val punctuation = mapOf(
            '!' to 1L,
            '¡' to 2L,
            '\'' to 3L,
            '(' to 4L,
            ')' to 5L,
            ',' to 6L,
            '-' to 7L,
            '.' to 8L,
            ':' to 9L,
            ';' to 10L,
            '¿' to 11L,
            '?' to 12L
        )

        vocabulary.putAll(punctuation)

        return text
            .lowercase()
            .mapNotNull { vocabulary[it] }
            .map { it }
            .toLongArray()
    }
}