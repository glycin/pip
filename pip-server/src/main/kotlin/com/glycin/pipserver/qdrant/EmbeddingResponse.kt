package com.glycin.pipserver.qdrant

data class EmbeddingResponse(
    val results: List<EmbeddingResult>
)

data class EmbeddingResult(
    val text: String,
    val dense: List<Float>,
    val sparse: Map<Int, Float>,
    val colbert: List<List<Float>>
)