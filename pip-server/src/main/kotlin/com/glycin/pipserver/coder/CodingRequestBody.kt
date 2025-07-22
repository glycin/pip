package com.glycin.pipserver.coder

data class CodingRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String? = null,
)