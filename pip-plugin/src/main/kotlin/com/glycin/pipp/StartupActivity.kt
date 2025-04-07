package com.glycin.pipp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class StartupActivity: ProjectActivity {

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().getService(PipService::class.java).init(project)
    }
}