package com.glycin.pipp.http

data class CodingRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String? = null,
)