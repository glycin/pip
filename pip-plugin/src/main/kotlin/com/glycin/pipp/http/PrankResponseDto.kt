package com.glycin.pipp.http

data class PipPrankResponseDto(
    val response: String,
    val type: PrankType,
    val code: String,
)

enum class PrankType {
    EXPLODE,
    OBFUSCATE,
    TRANSLATE,
}