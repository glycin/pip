package com.glycin.pipscripts.slack

data class SlackTrimmedMessage(
    val id: String,
    val timestamp: String,
    val userName: String,
    val message: String,
)