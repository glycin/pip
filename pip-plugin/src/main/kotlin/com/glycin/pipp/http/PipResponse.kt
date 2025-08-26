package com.glycin.pipp.http

data class PipResponse(
    val response: String,
    val prankType: String?,
    val code: List<CodeSnippets>?,
)

data class CodeSnippets(
    val className: String,
    val methodName: String?,
    val line: Int,
    val operation: CodeOperation = CodeOperation.INSERT,
    val code: String,
)

enum class CodeOperation {
    INSERT,
    REPLACE,
}