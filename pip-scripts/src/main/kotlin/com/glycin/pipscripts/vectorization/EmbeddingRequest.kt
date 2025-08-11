package com.glycin.pipscripts.vectorization

data class EmbeddingRequest(
    val texts: List<String>,
    val dense: Boolean = false,
    val sparse: Boolean = false,
    val colbert: Boolean = true,
)

data class EmbeddingResponse(
    val results: List<EmbeddingResult>
)

data class EmbeddingResult(
    val text: String,
    val dense: List<Double>,
    val sparse: Map<String, Double>,
    val colbert: List<List<Double>>
)