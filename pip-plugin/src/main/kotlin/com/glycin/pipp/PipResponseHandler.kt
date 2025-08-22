package com.glycin.pipp

import com.glycin.pipp.http.PipResponse
import com.glycin.pipp.utils.TextWriter
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class PipResponseHandler(
    private val editor: Editor,
    private val project: Project,
    private val scope: CoroutineScope,
    private val pip: Pip,
    private val agentComponent: AgentComponent,
) {

    fun processMusicResponse(pipResponse: PipResponse) {
        scope.launch(Dispatchers.EDT) {
            pip.changeStateTo(PipState.SITTING)
            delay(1500)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(10_000)
            agentComponent.hideSpeechBubble()
        }
    }

    fun processChatResponse(pipResponse: PipResponse) {
        scope.launch(Dispatchers.EDT) {
            pip.changeStateTo(PipState.WALL_SHOOTING)
            delay(500)
            agentComponent.showSpeechBubble(pipResponse.response)
            delay(30_000)
            agentComponent.hideSpeechBubble()
        }
    }

    fun processCodingResponse(response: PipResponse) {
        if(response.prankType.isNullOrEmpty()){
            processAcceptedCoding(response)
        } else {
            processPrank(response)
        }
    }

    private fun processAcceptedCoding(response: PipResponse) {
        val codeFragments = response.code ?: emptyList()

        scope.launch(Dispatchers.EDT) {
            delay(500)
            codeFragments.forEach { cf ->
                val validatedLine = if(cf.line >= editor.document.lineCount) 0 else cf.line
                val startOffset = editor.document.getLineStartOffset(validatedLine)
                val validatedEndOffset = min(startOffset + cf.code.length, editor.document.textLength)
                TextWriter.replaceText(startOffset, validatedEndOffset, cf.code, editor, project)
            }
            delay(1000)
            pip.changeStateTo(PipState.SITTING)
            agentComponent.showSpeechBubble(response.response)
        }
    }

    private fun processPrank(response: PipResponse) {
        scope.launch(Dispatchers.EDT) {
            delay(1000)
            pip.changeStateTo(PipState.SITTING)
        }
    }
}