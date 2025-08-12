package com.glycin.pipserver.coder

import com.glycin.pipserver.qdrant.EmbeddingRequest
import com.glycin.pipserver.qdrant.EmbeddingService
import com.glycin.pipserver.qdrant.QdrantService
import com.glycin.pipserver.util.NanoId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

private val LOG = KotlinLogging.logger {}

@Service
class CoderService(
    ollama: OllamaChatModel,
    chatMemory: ChatMemory,
    private val qdrantService: QdrantService,
) {
    private val pipCoder = ChatClient.builder(ollama)
        .defaultAdvisors(
            SimpleLoggerAdvisor(),
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
        )
        .build()

    fun generate(codingRequestBody: CodingRequestBody): String? {
        val additionalContext = qdrantService.search(codingRequestBody.input, "Riccardo").joinToString { it.text }
        LOG.info { additionalContext }
        return with(codingRequestBody) {
            pipCoder
                .prompt(Prompt("$input. Here some additional context that riccardo has said relevant to this matter $additionalContext"))
                .system("${Prompts.CODER_SYSTEM_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId ?: NanoId.generate()) }
                .call()
                .content()
        }
    }

    fun generateStream(codingRequestBody: CodingRequestBody): Flow<String> {
        return with(codingRequestBody) {
            pipCoder
                .prompt(Prompt(input))
                .system("${Prompts.CODER_SYSTEM_PROMPT} ${if (think) "/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId ?: NanoId.generate()) }
                .stream()
                .content()
                .chatFlow("Finished chatting")
        }
    }

    private fun Flux<String>.chatFlow(
        onCompleteMessage: String,
    ): Flow<String> {
        return callbackFlow {
            subscribe (
                { value ->
                    val encodedValue = Base64.getEncoder().encodeToString(value.toByteArray())
                    trySend(encodedValue).isSuccess
                },
                { error ->
                    LOG.error { error.message }
                    close(error)
                },
                {
                    LOG.info { onCompleteMessage }
                    close()
                }
            )

            awaitClose { }
        }
    }
}