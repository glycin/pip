package com.glycin.pipserver.shared

import com.glycin.pipserver.coder.CodeSnippets

data class CodeFragmentDto(
    val className: String,
    val methodName: String?,
    val line: Int,
    val operation: String,
    val code: String,
)

fun CodeSnippets.toDto() = CodeFragmentDto(
    className = className,
    methodName = methodName,
    line = line,
    operation = operation,
    code = code,
)