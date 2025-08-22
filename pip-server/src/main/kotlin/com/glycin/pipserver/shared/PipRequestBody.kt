package com.glycin.pipserver.shared

import com.glycin.pipserver.util.NanoId

data class PipRequestBody(
    val input: String,
    val think: Boolean = false,
    val chatId: String = NanoId.generate(),
    val category: String? = null,
    val categoryReason: String? = null,
)