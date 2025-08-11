package com.glycin.pipscripts.vectorization

import java.util.*

data class VectorDocument(
    val id: UUID,
    val timestamp: String,
    val userName: String,
    val text: String,
    val denseVector: List<Float>,
    val sparseVector: Map<Int, Float>,
    val colbertVectors: List<List<Float>>,
)