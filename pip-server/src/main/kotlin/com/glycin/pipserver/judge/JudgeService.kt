package com.glycin.pipserver.judge

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.qdrant.QdrantService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class JudgeService(
    ollama: OllamaChatModel,
    chatMemory: ChatMemory,
    private val qdrantService: QdrantService,
    @Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {
    private val judgeDredd = ChatClient.builder(ollama)
        .defaultAdvisors(
            SimpleLoggerAdvisor(),
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
        )
        .build()

    fun judge(pipRequestBody: PipRequestBody): JudgeAgentResponse? {
        LOG.info { "Handling request with id ${pipRequestBody.chatId}" }
        val judgment = with(pipRequestBody) {
            judgeDredd
                .prompt(Prompt(input))
                .system("${JudgePrompts.GENERIC_JUDGE} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return judgment?.let { raw ->
            val cleaned = raw.replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "").trim()
            parseToStructuredOutput(cleaned)
        }
    }

    private fun parseToStructuredOutput(response: String): JudgeAgentResponse? {
        return try {
            objectMapper.readValue(response, JudgeAgentResponse::class.java)
        } catch (e: Exception) {
            LOG.error{ "Failed to parse cleaned response $e"}
            null
        }
    }
}