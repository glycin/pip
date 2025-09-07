package com.glycin.pipserver.shared

data class PipPasteBody(
    val pasteText: String,
    val pasteLine: Int,
    val document: String,
    val chatId: String,
)