package com.glycin.pipp.http

data class PipRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String? = null,
)