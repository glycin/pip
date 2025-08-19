package com.glycin.pipserver.chatter

import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.shared.PipRequestBody
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service

@Service
class ChatterService(
    private val pipChatter: ChatClient,
) {

    fun judgmentalChat(request: PipRequestBody, judgment: JudgeAgentResponse): ChatterResponse? {
        return with(request) {
            pipChatter
                .prompt(Prompt("""
                    $input
                    The categorization agent had the following judgement: ${judgment.verdict}, because ${judgment.reason}
                """.trimIndent()))
                .system("${ChatterPrompts.CHATTER_GENERIC_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
                ?.let { ChatterResponse(it) }
        }
    }

    fun chat(request: PipRequestBody): ChatterResponse? {
        return with(request) {
            pipChatter
                .prompt(Prompt("""
                    $input
                """.trimIndent()))
                .system("${ChatterPrompts.CHATTER_GENERIC_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
                ?.let { ChatterResponse(it) }
        }
    }
}