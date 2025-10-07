package com.glycin.pipserver.judge

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.PrankType
import com.glycin.pipserver.shared.TrollAgentResponseDto
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
class JudgeService(
    @param:Qualifier("pip") private val judgeDredd: ChatClient,
    @param:Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

    fun categorize(pipRequestBody: PipRequestBody): CategorizationResponse? {
        LOG.info { "Categorization agent is handling request with id ${pipRequestBody.chatId}" }
        val categorization = with(pipRequestBody) {
            judgeDredd
                .prompt(Prompt(input))
                .system("${JudgePrompts.CATEGORIZATION_JUDGE} /no_think")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return categorization?.let { raw ->
            val rawWithoutThinkTags = raw.withoutThinkTags()
            objectMapper.parseToStructuredOutput<CategorizationResponse>(rawWithoutThinkTags) { e ->
                LOG.error { "Could not parse $rawWithoutThinkTags because ${e.message} " }
            }
        }
    }

    fun judge(pipRequestBody: PipRequestBody): JudgeAgentResponse? {
        LOG.info { "Judge agent is handling request with id ${pipRequestBody.chatId}" }
        val judgment = with(pipRequestBody) {
            judgeDredd
                .prompt(Prompt(input))
                .system("${JudgePrompts.GENERIC_JUDGE} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return judgment?.let { raw ->
            val rawWithoutThinkTags = raw.withoutThinkTags()
            objectMapper.parseToStructuredOutput<JudgeAgentResponse>(rawWithoutThinkTags){ e ->
                LOG.error { "Could not parse $rawWithoutThinkTags because ${e.message} " }
            }
        }
    }

    fun troll(pipRequestBody: PipRequestBody, judgement: JudgeAgentResponse): TrollAgentResponseDto? {
        LOG.info { "Troll agent is handling request with id ${pipRequestBody.chatId}" }
        val troll = judgeDredd
            .prompt(Prompt("Original user query: ${pipRequestBody.input}. Denial reason ${judgement.reason}"))
            .system("${JudgePrompts.TROLL} /no_think")
            .advisors { it.param(ChatMemory.CONVERSATION_ID, pipRequestBody.chatId) }
            .call()
            .content()

        return troll?.let { raw ->
            val rawWithoutThinkTags = raw.withoutThinkTags()
            objectMapper.parseToStructuredOutput<TrollAgentResponse>(rawWithoutThinkTags){ e ->
                LOG.error { "Could not parse $rawWithoutThinkTags because ${e.message} " }
            }?.let { res ->
                TrollAgentResponseDto(
                    prankType = PrankType.EXPLODE, //PrankType.entries.toTypedArray().random(),
                    response = res.response,
                    memeFileName = res.memeFileName
                )
            }
        }
    }
}