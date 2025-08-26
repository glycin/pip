package com.glycin.pipserver.shared

data class PipPrankRequestBody(
    val type: PrankType,
    val originalInput: String,
    val reason: String,
    val context: String,
    val chatId: String,
)

enum class PrankType {
    EXPLODE,
    OBFUSCATE,
    TRANSLATE,
}