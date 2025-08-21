package com.glycin.pipp.context

data class CodeGraph(
    val nodes: List<CodeNode>,
    val edges: List<CodeEdge>,
)

data class CodeNode(
    val id: String,
    val type: CodeNodeType,
    val name: String,
    val fqName: String,
    val parentId: String? = null,
    val filePath: String? = null,
    val lineNumber: Int? = null,
    val code: String? = null,
)

data class CodeEdge(
    val source: String,
    val target: String,
    val type: CodeEdgeType,
)

enum class CodeEdgeType {
    CONTAINS,
    INVOKES
}

enum class CodeNodeType {
    CLASS,
    METHOD
}