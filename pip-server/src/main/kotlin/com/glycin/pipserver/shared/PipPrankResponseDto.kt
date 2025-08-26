package com.glycin.pipserver.shared

data class PipPrankResponseDto(
    val response: String,
    val type: PrankType,
    val code: String,
)