package com.glycin.pipp

import com.glycin.pipp.settings.PipSettings
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class ResetActivity: IntentionAction, HighPriorityAction {

    override fun getText(): @IntentionName String {
        return "Pip broke, RESET RESET RESET!"
    }

    override fun isAvailable(p0: Project, p1: Editor?, p2: PsiFile?): Boolean = true

    override fun invoke(
        project: Project,
        editor: Editor,
        p2: PsiFile?
    ) {
        val application = ApplicationManager.getApplication()
        val config = application.getService(PipSettings::class.java)
        val mainService = project.getService(MainService::class.java)
        mainService?.reset(editor, config)

    }

    override fun startInWriteAction(): Boolean = false
    override fun getFamilyName(): @IntentionFamilyName String = "Pip"
}