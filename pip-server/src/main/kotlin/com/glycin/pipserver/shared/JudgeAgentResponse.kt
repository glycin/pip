package com.glycin.pipserver.shared

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class JudgeAgentResponse(
    @param:JsonPropertyDescription("Either 'allow' or 'deny'.")
    val verdict: String,
    @param:JsonPropertyDescription("Short reasoning for the verdict. Paraphrase the user — do not embed raw double-quoted fragments.")
    val reason: String,
)
