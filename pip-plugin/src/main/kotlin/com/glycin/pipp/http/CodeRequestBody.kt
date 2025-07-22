package com.glycin.pipp.http

import kotlinx.serialization.Serializable

@Serializable
data class CodingRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String? = null,
)