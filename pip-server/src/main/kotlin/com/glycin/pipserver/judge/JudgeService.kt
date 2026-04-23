package com.glycin.pipserver.judge

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.PrankType
import com.glycin.pipserver.shared.TrollAgentResponseDto
import com.glycin.pipserver.util.Emojis
import com.glycin.pipserver.util.NanoId
import com.glycin.pipserver.util.withoutThinkTags
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class JudgeService(
    @param:Qualifier("pip_toolless") private val judgeDredd: ChatClient,
    @param:Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

    private val judgeConverter = BeanOutputConverter(JudgeAgentResponse::class.java, objectMapper)
    private val categorizationConverter = BeanOutputConverter(CategorizationResponse::class.java, objectMapper)
    private val trollConverter = BeanOutputConverter(TrollAgentResponse::class.java, objectMapper)

    fun categorize(pipRequestBody: PipRequestBody): CategorizationResponse? {
        LOG.info { "Categorization agent ${Emojis.nerdFace} is handling request with id ${pipRequestBody.chatId}" }
        val categorization = with(pipRequestBody) {
            judgeDredd
                .prompt(Prompt(input))
                .system(systemPrompt(JudgePrompts.CATEGORIZATION_JUDGE, categorizationConverter, think = false))
                .advisors { it.param(ChatMemory.CONVERSATION_ID, NanoId.generate()) }
                .call()
                .content()
        }

        return categorization?.let { raw -> parse(raw, categorizationConverter) }
    }

    fun judge(pipRequestBody: PipRequestBody): JudgeAgentResponse? {
        LOG.info { "Judge agent ${Emojis.judge} is handling request with id ${pipRequestBody.chatId}" }
        val judgment = with(pipRequestBody) {
            judgeDredd
                .prompt(Prompt(input))
                .system(systemPrompt(JudgePrompts.GENERIC_JUDGE, judgeConverter, think = think))
                .advisors { it.param(ChatMemory.CONVERSATION_ID, NanoId.generate()) }
                .call()
                .content()
        }

        return judgment?.let { raw -> parse(raw, judgeConverter) }
    }

    fun troll(pipRequestBody: PipRequestBody, judgement: JudgeAgentResponse): TrollAgentResponseDto? {
        LOG.info { "Troll agent ${Emojis.troll} is handling request with id ${pipRequestBody.chatId}" }
        val troll = judgeDredd
            .prompt(Prompt("Original user query: ${pipRequestBody.input}. Denial reason ${judgement.reason}"))
            .system(systemPrompt(JudgePrompts.TROLL, trollConverter, think = false))
            .advisors { it.param(ChatMemory.CONVERSATION_ID, pipRequestBody.chatId) }
            .call()
            .content()

        return troll?.let { raw -> parse(raw, trollConverter) }?.let { res ->
            TrollAgentResponseDto(
                prankType = PrankType.EXPLODE, //PrankType.entries.toTypedArray().random(),
                response = res.response,
                memeFileName = res.memeFileName
            )
        }
    }

    private fun systemPrompt(base: String, converter: BeanOutputConverter<*>, think: Boolean): String =
        "$base\n\n${converter.format}\n\n${if (think) "/think" else "/no_think"}"

    private fun <T> parse(raw: String, converter: BeanOutputConverter<T>): T? {
        val cleaned = raw.withoutThinkTags()
        return try {
            converter.convert(cleaned)
        } catch (e: Exception) {
            LOG.error { "Could not parse $cleaned because ${e.message}" }
            null
        }
    }
}
