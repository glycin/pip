package com.glycin.pipp.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class PipConfigurable: SearchableConfigurable {

    private var panel: JPanel? = null
    private var pathField: TextFieldWithBrowseButton? = null
    private val settings = ApplicationManager.getApplication().getService(PipSettings::class.java)

    override fun createComponent(): JComponent? {
        panel = JPanel().apply {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.anchor = GridBagConstraints.NORTHWEST
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            gbc.insets = JBUI.insetsBottom(5)

            gbc.gridy = 0
            val titleLabel = JLabel("Your personal intelligent pair-programmer.")
            add(titleLabel, gbc)

            gbc.gridy = 1
            val separator = JSeparator()
            add(separator, gbc)

            gbc.gridy = 2
            gbc.fill = GridBagConstraints.NONE

            val infoLabel = JLabel("Path to save context json to.")
            add(infoLabel, gbc)

            gbc.gridy = 3
            pathField = TextFieldWithBrowseButton().apply {
                textField.columns = 30 // similar to DSL .columns(30)
                text = settings.state.jsonExportPath.orEmpty()
                addBrowseFolderListener(
                    /* title    = */ "Select Json File",
                    /* description = */ "Select json file path to save graph to.",
                    /* project = */ null,
                    /* fileChooserDescriptor = */ FileChooserDescriptorFactory.createSingleFileDescriptor()
                )
            }
            add(pathField!!, gbc)

            gbc.gridy = 4
            val s2 = JSeparator()
            add(s2, gbc)

            gbc.weighty = 1.0
            add(Box.createVerticalGlue(), gbc)
        }

        return panel!!
    }

    override fun isModified(): Boolean = pathField?.text != settings.state.jsonExportPath

    override fun apply() {
        settings.state.jsonExportPath = pathField?.text ?: ""
    }

    override fun reset() {
        pathField?.text = ""
    }

    override fun getDisplayName(): String = "Settings For P.I.P"

    override fun getId(): String = "PipSettings"

    override fun disposeUIResources() {
        panel = null
    }
}