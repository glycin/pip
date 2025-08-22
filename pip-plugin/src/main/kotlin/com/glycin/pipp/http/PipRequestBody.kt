package com.glycin.pipp.http

data class PipRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String? = null,
    val category: RequestCategory? = null,
    val categoryReason: String? = null,
)