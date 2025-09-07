package com.glycin.pipp.http

data class PipPasteBody(
    val pasteText: String,
    val pasteLine: Int,
    val document: String,
    val chatId: String,
)