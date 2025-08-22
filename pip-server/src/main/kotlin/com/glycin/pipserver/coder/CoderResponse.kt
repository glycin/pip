package com.glycin.pipserver.coder

data class CoderResponse(
    val response: String,
    val codeSnippets: List<CodeSnippets>,
)

data class CodeSnippets(
    val className: String,
    val line: Int,
    val code: String,
)