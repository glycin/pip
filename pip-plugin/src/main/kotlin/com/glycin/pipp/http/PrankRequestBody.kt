package com.glycin.pipp.http

data class PipPrankRequestBody(
    val type: PrankType,
    val originalInput: String,
    val reason: String,
    val context: String,
    val chatId: String,
)