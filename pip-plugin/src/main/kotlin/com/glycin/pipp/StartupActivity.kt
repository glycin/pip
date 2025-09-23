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

class StartupActivity: IntentionAction, HighPriorityAction {

    private var mainService : MainService? = null

    override fun getText(): @IntentionName String {
        return if(mainService == null) {
            "Time for a red pill..."
        } else {
            "I need you P.I.P!"
        }
    }

    override fun isAvailable(p0: Project, p1: Editor?, p2: PsiFile?): Boolean = true

    override fun invoke(
        project: Project,
        editor: Editor,
        p2: PsiFile?
    ) {
        if(mainService == null) {
            val application = ApplicationManager.getApplication()
            val config = application.getService(PipSettings::class.java)
            mainService = application.getService(MainService::class.java)
            mainService?.init(project, editor, config)
        } else {
            mainService?.refocus(editor)
        }
    }

    override fun startInWriteAction(): Boolean = false
    override fun getFamilyName(): @IntentionFamilyName String = "Pip"
}