package com.glycin.pipserver

import com.glycin.pipserver.chatter.ChatterResponse
import com.glycin.pipserver.chatter.ChatterService
import com.glycin.pipserver.coder.CoderResponse
import com.glycin.pipserver.coder.CoderService
import com.glycin.pipserver.judge.JudgeService
import com.glycin.pipserver.judge.TrollAgentResponse
import com.glycin.pipserver.shared.CategorizationDto
import com.glycin.pipserver.shared.PipRequestBody
import com.glycin.pipserver.shared.PipResponse
import com.glycin.pipserver.shared.toDto
import com.glycin.pipserver.util.withoutThinkTags
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class PipService(
    private val coderService: CoderService,
    private val judgeService: JudgeService,
    private val chatService: ChatterService,
) {

    fun categorize(request: PipRequestBody): CategorizationDto? {
        return judgeService.categorize(request)?.toDto()
    }

    fun requestHelp(request: PipRequestBody): PipResponse {
        request.category?.let { category ->
            LOG.info { "Categorized response: $category (${request.categoryReason})" }
            return when(category.uppercase()) {
                "CODING" -> codingRequest(request)
                "JUST_CHATTING" -> chattingRequest(request)
                "GAMES" -> PipResponse.UNSUPPORTED_RESPONSE
                "MUSIC" -> musicRequest(request)
                else -> PipResponse.UNKNOWN_RESPONSE
            }
        } ?: return PipResponse.FAIL_RESPONSE
    }

    private fun codingRequest(request: PipRequestBody): PipResponse {
        val judgment = judgeService.judge(request)
        judgment?.let {
            LOG.info { "Judge Dredd says: ${judgment.verdict} because ${judgment.reason}" }
            LOG.info { "${request.input}" }
            return when (it.verdict.lowercase()) {
                "deny", "denial" -> {
                    judgeService.troll(request, it)?.toResponse() ?: PipResponse.FAIL_RESPONSE
                }

                "accept", "acceptance" -> {
                    coderService.generate(request, it)?.toResponse() ?: PipResponse.FAIL_RESPONSE
                }

                else -> {
                    LOG.warn { "Agent gave wrong verdict: ${it.verdict.lowercase()}" }
                    PipResponse.FAIL_RESPONSE
                }
            }
        } ?: return PipResponse.FAIL_RESPONSE
    }

    private fun chattingRequest(request: PipRequestBody): PipResponse {
        val judgment = judgeService.judge(request)
        return judgment?.let {
            LOG.info { "Judge Dredd says: chat request is ${judgment.verdict} because ${judgment.reason}" }
            chatService.judgmentalChat(request, judgment)?.toResponse()
        } ?: PipResponse.FAIL_RESPONSE
    }

    private fun musicRequest(request: PipRequestBody): PipResponse {
        return chatService.chat(request)?.toResponse() ?: PipResponse.FAIL_RESPONSE
    }
    
    private fun ChatterResponse.toResponse() = PipResponse(
        response = response,
        prankType = null,
        code = null,
    )

    private fun TrollAgentResponse.toResponse() = PipResponse(
        response = response,
        prankType = trollMode,
        code = null,
    )

    private fun CoderResponse.toResponse() = PipResponse(
        response = response,
        prankType = null,
        code = codeSnippets.map { it.toDto() }
    )
}