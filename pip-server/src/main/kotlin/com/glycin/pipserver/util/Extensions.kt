package com.glycin.pipserver.util

import com.fasterxml.jackson.databind.ObjectMapper

fun String.withoutThinkTags() = replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "").trim()

fun String.getThinkText() = Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL).find(this)?.value

private val OUTER_FENCE = Regex("^```(?:[a-zA-Z0-9_-]+)?\\s*\\n(.*)\\n```\\s*$", RegexOption.DOT_MATCHES_ALL)

fun String.withoutCodeFences(): String {
    val trimmed = trim()
    return OUTER_FENCE.matchEntire(trimmed)?.groupValues?.get(1)?.trim() ?: trimmed
}

inline fun <reified T> ObjectMapper.parseToStructuredOutput(
    response: String,
    onError: (Exception) -> Unit = {}
): T? {
    return try {
        readValue(response.withoutCodeFences(), T::class.java)
    } catch (e: Exception) {
        onError(e)
        null
    }
}