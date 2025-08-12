package com.glycin.pipserver.qdrant

data class EmbeddingRequest(
    val texts: List<String>,
    val dense: Boolean = true,
    val sparse: Boolean = true,
    val colbert: Boolean = true,
)