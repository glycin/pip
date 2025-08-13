package com.glycin.pipserver

import com.glycin.pipserver.coder.CoderService
import com.glycin.pipserver.judge.JudgeService
import com.glycin.pipserver.shared.PipRequestBody
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val LOG = KotlinLogging.logger {}

@Service
class PipService(
    private val coderService: CoderService,
    private val judgeService: JudgeService,
) {

    fun requestHelp(request: PipRequestBody): String? {
        val judgment = judgeService.judge(request)

        judgment?.let {
            LOG.info { "Judge Dredd says: ${judgment.verdict} because ${judgment.reason}" }
            return "hype"
        } ?: return null
    }
}