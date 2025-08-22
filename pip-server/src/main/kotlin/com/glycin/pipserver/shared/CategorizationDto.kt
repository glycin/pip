package com.glycin.pipserver.shared

import com.glycin.pipserver.judge.CategorizationResponse

data class CategorizationDto(
    val category: String,
    val reason: String,
)

fun CategorizationResponse.toDto() = CategorizationDto(category, reason)