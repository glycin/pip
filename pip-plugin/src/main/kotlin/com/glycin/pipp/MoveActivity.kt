package com.glycin.pipp

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class MoveActivity: IntentionAction, HighPriorityAction {

    override fun getText(): @IntentionName String {
        return "God pip you are in the way, move!"
    }

    override fun isAvailable(p0: Project, p1: Editor?, p2: PsiFile?): Boolean = true

    override fun invoke(
        project: Project,
        editor: Editor,
        p2: PsiFile?
    ) {
        val mainService = project.getService(MainService::class.java)
        mainService?.movePipToMiddle()
    }

    override fun startInWriteAction(): Boolean = false
    override fun getFamilyName(): @IntentionFamilyName String = "Pip"
}