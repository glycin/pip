package com.glycin.pipp

import com.glycin.pipp.settings.PipSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class StartupActivity: ProjectActivity {

    override suspend fun execute(project: Project) {
        val application = ApplicationManager.getApplication()
        val config = application.getService(PipSettings::class.java)
        application.getService(MainService::class.java).init(project, config)
    }
}