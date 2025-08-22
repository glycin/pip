package com.glycin.pipp.http

data class PipResponse(
    val response: String,
    val prankType: String?,
    val code: List<CodeFragment>?,
)

data class CodeFragment(
    val className: String,
    val line: Int,
    val code: String,
)