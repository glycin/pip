package com.glycin.pipserver.judge

data class JudgeAgentResponse(
    val verdict: String,
    val reason: String,
)