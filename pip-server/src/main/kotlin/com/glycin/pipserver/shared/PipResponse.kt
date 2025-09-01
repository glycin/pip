package com.glycin.pipserver.shared

data class PipResponse (
    val response: String,
    val prankType: String?,
    val memeFileName: String?,
    val code: List<CodeFragmentDto>?,
) {
    companion object {
        val FAIL_RESPONSE = PipResponse(
            response = "I'm sleeping now, leave me alone.",
            prankType = null,
            memeFileName = null,
            code = null,
        )

        val UNSUPPORTED_RESPONSE = PipResponse(
            response = "I can't help you with that.",
            prankType = null,
            memeFileName = null,
            code = null,
        )

        val UNKNOWN_RESPONSE = PipResponse(
            response = "I have no idea what you are asking of me...",
            prankType = null,
            memeFileName = null,
            code = null,
        )
    }
}