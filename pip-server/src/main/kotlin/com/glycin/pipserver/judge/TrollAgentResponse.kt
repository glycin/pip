package com.glycin.pipserver.judge

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class TrollAgentResponse(
    @param:JsonPropertyDescription("The sarcastic, snarky response to the user.")
    val response: String,
    @param:JsonPropertyDescription("File name of a generated meme. Null unless a meme was actually generated.")
    val memeFileName: String?,
)
