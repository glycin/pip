package com.glycin.pipserver.chatter

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.util.parseToStructuredOutput
import com.glycin.pipserver.util.withoutThinkTags
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class ChatterService(
    @param:Qualifier("pip") private val pipChatter: ChatClient,
    @param:Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

    fun judgmentalChat(request: PipRequestBody, judgment: JudgeAgentResponse): ChatterResponse? {
        val response = with(request) {
            pipChatter
                .prompt(Prompt("""
                    $input
                    The categorization agent had the following judgement: ${judgment.verdict}, because ${judgment.reason}
                """.trimIndent()))
                .system("${ChatterPrompts.CHATTER_GENERIC_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return response?.let {
            val raw = it.withoutThinkTags()
            objectMapper.parseToStructuredOutput<ChatterResponse>(raw) { e ->
                LOG.error { "Could not parse $raw because ${e.message} " }
            }
        }
    }

    fun chat(request: PipRequestBody): ChatterResponse? {
        return with(request) {
            pipChatter
                .prompt(Prompt("""
                    $input
                """.trimIndent()))
                .system("${ChatterPrompts.MUSICIAN_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
                ?.let { ChatterResponse(it.withoutThinkTags(), null) }
        }
    }

    fun game(request: PipRequestBody): GamerResponse? {
        val response = with(request) {
            pipChatter
                .prompt(Prompt("""
                    $input
                """.trimIndent()))
                .system("${ChatterPrompts.GAMER_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return response?.let {
            val raw = it.withoutThinkTags()
            objectMapper.parseToStructuredOutput<GamerResponse>(raw) { e ->
                LOG.error { "Could not parse gamer response $raw because ${e.message} " }
            }
        }
    }
}