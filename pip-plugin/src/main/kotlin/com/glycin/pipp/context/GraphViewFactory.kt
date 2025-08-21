package com.glycin.pipp.context

import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel

class GraphViewFactory: ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            throw IllegalStateException("JCEF is not supported on this platform.")
        }

        val browser = JBCefBrowser().apply {
            loadURL("http://localhost:5173/")
        }

        val panel = JPanel().apply {
            layout = BorderLayout()
            add(browser.component, BorderLayout.CENTER)
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)

        toolWindow.contentManager.addContent(content)
    }

    private fun computeGraphAsync(project: Project, onDone: (String) -> Unit) {
        DumbService.getInstance(project).runReadActionInSmartMode {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    val (nodes, edges) = CodeGraphBuilder(project).build()
                    val json = GsonBuilder().disableHtmlEscaping().create()
                        .toJson(mapOf("nodes" to nodes, "links" to edges))
                    ApplicationManager.getApplication().invokeLater { onDone(json) }
                },
                "Building PSI Graph",
                true,
                project
            )
        }
    }
}