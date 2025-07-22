package com.glycin.pipp.prompts

object CodingPrompts {

    fun generateCodeRequestWithContext(question: String, context: String): String {
        return """
            $question
            And here the added code context regarding the previous query:
            $context
        """.trimIndent()
    }
}