package com.glycin.pipserver.judge

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class CategorizationResponse(
    @param:JsonPropertyDescription("One of: JUST_CHATTING, CODING, GAMES, MUSIC, BUTLER, STUCK.")
    val category: String,
    @param:JsonPropertyDescription("Short reasoning for the chosen category.")
    val reason: String,
)
