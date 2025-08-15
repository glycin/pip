package com.glycin.pipserver.coder

import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.qdrant.QdrantService
import com.glycin.pipserver.shared.PipRequestBody
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

private val LOG = KotlinLogging.logger {}

@Service
class CoderService(
    private val pipCoder: ChatClient,
    private val qdrantService: QdrantService,
) {

    fun generate(
        pipRequestBody: PipRequestBody,
        judgement: JudgeAgentResponse = JudgeAgentResponse("accept", "good question")
    ): String? {
        val additionalContext = qdrantService.search(pipRequestBody.input, "Riccardo").joinToString { it.text }
        LOG.info { additionalContext }
        return with(pipRequestBody) {
            pipCoder
                .prompt(Prompt("""
                    $input.
                    The validation agent said this about the query: ${judgement.reason}.
                    Here some additional context that riccardo has said relevant to this matter $additionalContext taken from a chat log. Use only if relevant.
                """.trimIndent()))
                .system("${CoderPrompts.CODER_SYSTEM_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }
    }

    fun generateStream(
        pipRequestBody: PipRequestBody,
        judgement: JudgeAgentResponse = JudgeAgentResponse("accept", "good question")
    ): Flow<String> {
        val additionalContext = qdrantService.search(pipRequestBody.input, "Riccardo").joinToString { it.text }
        return with(pipRequestBody) {
            pipCoder
                .prompt(Prompt("""
                    $input.
                    The validation agent said this about the query: ${judgement.reason}.
                    Here some additional context that riccardo has said relevant to this matter $additionalContext taken from a chat log. Use only if relevant.
                """.trimIndent()))                .system("${CoderPrompts.CODER_SYSTEM_PROMPT} ${if (think) "/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
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