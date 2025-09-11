package com.glycin.pipserver.shared

import com.glycin.pipserver.chatter.TicTacToeResponse

data class PipTicTacToeResponseDto(
    val response: String,
    val move: Int,
) {
    companion object {
        val FAIL_RESPONSE = PipTicTacToeResponseDto(
            response = "I'm sleepy. I'm not gonna entertain you now. Here I win, easy, gg wp, get rekt n00b.",
            move = 5
        )
    }
}

fun TicTacToeResponse.toDto() = PipTicTacToeResponseDto(
    response = response,
    move = move
)