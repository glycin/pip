package com.glycin.pipserver.shared

data class TrollAgentResponseDto (
    val prankType: PrankType,
    val response: String,
    val memeFileName: String?,
)