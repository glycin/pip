package com.glycin.pipp.utils

import com.intellij.openapi.ui.popup.JBPopupFactory
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

fun showPngInPopup(owner: JComponent, imagePath: String, title: String = "Hhehehehehehe") {
    val img: BufferedImage = runCatching {
        ImageIO.read(File(imagePath))
    }.getOrNull() ?: return

    val label = JLabel(ImageIcon(img))
    val panel = JPanel()
    panel.add(label)

    val builder = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(panel, label)
        .setFocusable(true)
        .setRequestFocus(true)
        .setResizable(true)
        .setMovable(true)
        .setCancelOnClickOutside(true)
        .setCancelOnOtherWindowOpen(true)
        .setCancelKeyEnabled(true)

    val popup = if (title.isNotBlank()) builder.setTitle(title).createPopup()
    else builder.createPopup()

    popup.showInCenterOf(owner)
}