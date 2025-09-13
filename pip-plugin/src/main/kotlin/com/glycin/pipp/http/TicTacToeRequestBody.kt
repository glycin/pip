package com.glycin.pipp.http

data class TicTacToeRequestBody(
    val playerMoves: String,
    val aiMoves: String,
    val think: Boolean = false,
    val chatId: String,
)