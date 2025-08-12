package com.glycin.pipserver.qdrant

import java.time.LocalDateTime

data class QdrantSearchDto(
    val text: String,
    val timestamp: LocalDateTime,
    val userName: String,
    val score: Float,
)