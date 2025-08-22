package com.glycin.pipserver.shared

import com.glycin.pipserver.coder.CodeSnippets

data class CodeFragmentDto(
    val className: String,
    val line: Int,
    val code: String,
)

fun CodeSnippets.toDto() = CodeFragmentDto(
    className = className,
    line = line,
    code = code,
)