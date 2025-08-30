package com.glycin.pipserver.coder

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.qdrant.QdrantService
import com.glycin.pipserver.shared.PipPrankRequestBody
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.util.NanoId
import com.glycin.pipserver.util.getThinkText
import com.glycin.pipserver.util.parseToStructuredOutput
import com.glycin.pipserver.util.withoutThinkTags
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

private val LOG = KotlinLogging.logger {}

@Service
class CoderService(
    @param:Qualifier("pip_coder") private val pipCoder: ChatClient,
    private val qdrantService: QdrantService,
    @param:Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

    fun generate(
        pipRequestBody: PipRequestBody,
        judgement: JudgeAgentResponse = JudgeAgentResponse("accept", "good question")
    ): CoderResponse? {
        val additionalContext = qdrantService.search(pipRequestBody.input, "Riccardo")
            .mapNotNull { t ->
                t.takeUnless { it.text.isEmpty() }
            }
            .joinToString { it.text }
        //LOG.info { "Additional context: $additionalContext" }
        val response = with(pipRequestBody) {
            pipCoder
                .prompt(Prompt("""
                    $input
                    The validation agent said this about the query: ${judgement.reason}.
                    ${if(additionalContext.isEmpty()) "" else "Here some additional context that riccardo has said relevant to this matter $additionalContext taken from a chat log. Use only if relevant." }
                """.trimIndent()))
                .system("${CoderPrompts.CODER_SYSTEM_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return response?.let { raw ->
            val thinkingTags = raw.getThinkText()
            val rawWithoutThink = raw.withoutThinkTags()
            objectMapper.parseToStructuredOutput<CoderResponse>(rawWithoutThink) { e ->
                LOG.info { "Could not parse $rawWithoutThink because ${e.message}" }
            }
        }
    }

    fun generateObfuscationPrank(pipPrankRequestBody: PipPrankRequestBody): PrankerResponse? {
        LOG.info{ "Obfuscating ${pipPrankRequestBody.context}" }
        val obfuscatedCode = with(pipPrankRequestBody) {
            pipCoder
                .prompt(Prompt("""
                    Completely rewrite the provided code so that it's a messy unreadable mess.
                    $context
                """.trimIndent()))
                .system("${CoderPrompts.CODE_OBFUSCATOR_PROMPT} /no_think") //TODO: This aint working, maybe have it write a haiku or a poem?
                .advisors { it.param(ChatMemory.CONVERSATION_ID, NanoId.generate()) }
                .call()
                .content()
        }
        LOG.info { obfuscatedCode }
        return generatePrank(pipPrankRequestBody, obfuscatedCode?.withoutThinkTags() ?: "")
    }

    fun generateTranslationPrank(pipPrankRequestBody: PipPrankRequestBody): PrankerResponse? {
        LOG.info{ "Translating  ${pipPrankRequestBody.context}" }
        val language = listOf("GREEK", "ITALIAN", "JAPANESE", "CHINESE").random()
        val translatedCode = with(pipPrankRequestBody) {
            pipCoder
                .prompt(Prompt(
                    """
                        Translate this to $language:
                        $context
                    """.trimIndent()
                ))
                .system("${CoderPrompts.CODE_TRANSLATOR_PROMPT} /no_think")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, NanoId.generate()) }
                .call()
                .content()
        }
        LOG.info { translatedCode }
        return generatePrank(pipPrankRequestBody, translatedCode?.withoutThinkTags() ?: "")
    }

    private fun generatePrank(pipPrankRequestBody: PipPrankRequestBody, prankedCode: String): PrankerResponse? {
        LOG.info { "Generating prank ${pipPrankRequestBody.type}" }
        val additionalContext = qdrantService.search(pipPrankRequestBody.reason, null)
            .mapNotNull { t ->
                t.takeUnless { it.text.isEmpty() }
            }
            .joinToString { it.text }

        val response = with(pipPrankRequestBody) {
            pipCoder
                .prompt(Prompt("""
                    The original query is:
                    $originalInput
                    But a judge agent declined to help with the following reason: $reason.
                    Let them know how you think about them!
                    ${if(additionalContext.isEmpty()) "" else "Here some additional context that has been has said that could be relevant to this matter $additionalContext taken from a chat log. Use only if relevant." }
                """.trimIndent()))
                .system("${CoderPrompts.CODE_PRANKER_SYSTEM_PROMPT} /no_think")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return response?.let { raw ->
            val rawWithoutThink = raw.withoutThinkTags()
            PrankerResponse(
                response = rawWithoutThink,
                code = prankedCode,
            )
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
                """.trimIndent()))
                .system("${CoderPrompts.CODER_SYSTEM_PROMPT} ${if (think) "/think" else "/no_think"}")
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