package com.glycin.pipserver.chatter

import com.fasterxml.jackson.databind.ObjectMapper
import com.glycin.pipserver.shared.JudgeAgentResponse
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.TicTacToeRequestBody
import com.glycin.pipserver.util.Emojis
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
    @param:Qualifier("pip_toolless") private val pipToolless: ChatClient,
    @param:Qualifier("pipObjectMapper") private val objectMapper: ObjectMapper,
) {

    fun judgmentalChat(request: PipRequestBody, judgment: JudgeAgentResponse): ChatterResponse? {
        LOG.info { "Judgy chatting agent ${Emojis.chatter} is handling chatting request with id ${request.chatId}" }
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
        LOG.info { "Chatting agent ${Emojis.chatter} is handling chatting request with id ${request.chatId}" }
        val response = with(request) {
            pipChatter
                .prompt(Prompt(input))
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

    fun chatMusic(request: PipRequestBody): ChatterResponse? {
        LOG.info { "Music chatting agent ${Emojis.rocker} is handling music request with id ${request.chatId}" }
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
        LOG.info { "Gaming chatting agent ${Emojis.gamer} is handling request with id ${request.chatId}" }
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

    fun ticTacToe(request: TicTacToeRequestBody): TicTacToeResponse? {
        LOG.info { "Playing tic tac toe: User placed on ${request.playerMoves}" }
        val response = with(request) {
            pipToolless
                .prompt(Prompt("""
                    The user placed an X marker on the following cells: ${request.playerMoves}.
                    You have placed a marker on the following cells: ${request.aiMoves}.
                    What is your next move?
                """.trimIndent()))
                .system("${ChatterPrompts.TIC_TAC_TOE_PROMPT} ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }
                .call()
                .content()
        }

        return response?.let {
            val raw = it.withoutThinkTags()
            objectMapper.parseToStructuredOutput<TicTacToeResponse>(raw) { e ->
                LOG.error { "Could not parse tic tac toe response $raw because ${e.message} " }
            }
        }
    }

    fun chatStuck(request: PipRequestBody): ChatterResponse? {
        LOG.info { "${Emojis.butler} is helping the user with their little stuck problem." }
        val response = with(request) {
            pipChatter
                .prompt(Prompt("""
                    The user is stuck because ${request.categoryReason}.
                    Respond with telling the user that you will hypnotize them using some next level hypnosis tactics.
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
}