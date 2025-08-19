package com.glycin.pipserver

import com.glycin.pipserver.chatter.ChatterService
import com.glycin.pipserver.coder.CoderService
import com.glycin.pipserver.judge.JudgeService
import com.glycin.pipserver.shared.PipRequestBody
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

private const val FAIL_RESPONSE = "I'm sleeping now, leave me alone."
@Service
class PipService(
    private val coderService: CoderService,
    private val judgeService: JudgeService,
    private val chatService: ChatterService,
) {

    fun requestHelp(request: PipRequestBody): String? {
        judgeService.categorize(request)?.let { category ->
            LOG.info { "Categorized response: ${category.category} (${category.reason})" }
            return when(category.category.uppercase()) {
                "CODING" -> codingRequest(request)
                "JUST_CHATTING" -> chattingRequest(request)
                "GAMES" -> "NOT YET SUPPORTED"
                "MUSIC" -> musicRequest(request)
                else -> "UNKNOWN CATEGORY"
            }
        } ?: return null
    }

    private fun codingRequest(request: PipRequestBody): String? {
        val judgment = judgeService.judge(request)

        judgment?.let {
            LOG.info { "Judge Dredd says: ${judgment.verdict} because ${judgment.reason}" }
            return when (it.verdict.lowercase()) {
                "deny", "denial" -> {
                    judgeService.troll(request, it)?.response
                }

                "accept", "acceptance" -> {
                    coderService.generate(request, it)
                }

                else -> {
                    LOG.warn { "Agent gave wrong verdict: ${it.verdict.lowercase()}" }
                    FAIL_RESPONSE
                }
            }
        } ?: return FAIL_RESPONSE
    }

    private fun chattingRequest(request: PipRequestBody): String {
        val judgment = judgeService.judge(request)
        return judgment?.let {
            LOG.info { "Judge Dredd says: chat request is ${judgment.verdict} because ${judgment.reason}" }
            chatService.judgmentalChat(request, judgment)?.response
        } ?: FAIL_RESPONSE
    }

    private fun musicRequest(request: PipRequestBody): String {
        return chatService.chat(request)?.response ?: FAIL_RESPONSE
    }
}