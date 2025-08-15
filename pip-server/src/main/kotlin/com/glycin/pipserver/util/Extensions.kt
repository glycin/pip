package com.glycin.pipserver.util

fun String.withoutThinkTags() = replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "").trim()