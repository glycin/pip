package com.glycin.pipserver.util

import com.fasterxml.jackson.databind.ObjectMapper

fun String.withoutThinkTags() = replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "").trim()

fun String.getThinkText() = Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL).find(this)?.value

inline fun <reified T> ObjectMapper.parseToStructuredOutput(
    response: String,
    onError: (Exception) -> Unit = {}
): T? {
    return try {
        readValue(response, T::class.java)
    } catch (e: Exception) {
        onError(e)
        null
    }
}