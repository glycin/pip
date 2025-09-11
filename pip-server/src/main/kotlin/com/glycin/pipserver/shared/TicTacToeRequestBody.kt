package com.glycin.pipserver.shared

data class TicTacToeRequestBody(
    val playerMoves: String,
    val aiMoves: String,
    val think: Boolean = false,
    val chatId: String,
)