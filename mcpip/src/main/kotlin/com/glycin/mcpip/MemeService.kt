package com.glycin.mcpip

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.awt.*
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.AttributedString
import javax.imageio.ImageIO
import java.util.Base64
import java.util.UUID

@Service
class MemeService(
    private val memeProperties: MemeProperties,
) {

    private val log = LoggerFactory.getLogger(SpotifyService::class.java)

    @Tool(description = "Generate the 'one does not simply' meme. Use when you need a meme to indicate the user is wants to do something very hard.")
    fun memeNotSimply(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE ONE DOES NOT SIMPLY MEME INVOKED => $topText / $bottomText")
        return generateMemeBase64("one_does_not_simply.png", topText, bottomText)
    }

    @Tool(description = "Generate the 'ancient aliens' meme. Use when you are paranoid about something.")
    fun memeAncientAliens(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE ANCIENT ALIENS MEME INVOKED => $topText / $bottomText")
        return generateMemeBase64("ancient_aliens.png", topText, bottomText)
    }

    @Tool(description = "Generate the 'sad pablo escobar' meme. Use when you or the user are waiting for something for a long time.")
    fun memeSadPabloEscobar(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE SAD PABLO ESCOBAR INVOKED => $topText / $bottomText")
        return generateMemeBase64("sad_pablo.png", topText, bottomText)
    }

    @Tool(description = "Generate the 'hide the pain' meme. Use when you or the user are sad and are trying to hide it.")
    fun memeHideThePain(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE HIDE THE PAIN INVOKED => $topText / $bottomText")
        return generateMemeBase64("hide_the_pain.png", topText, bottomText)
    }

    @Tool(description = "Generate the 'roll safe' meme. Use when you want to indicate something clever.")
    fun memeThinkAboutIt(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE ROLL SAFE INVOKED => $topText / $bottomText")
        return generateMemeBase64("roll_safe.jpg", topText, bottomText)
    }

    @Tool(description = "Generate the 'challenge accepted' meme. Use when you want to indicate a challenge is being taken on!")
    fun memeChallengeAccepted(
        @ToolParam(description = "The top text for the meme") topText: String,
        @ToolParam(description = "The bottom text for the meme") bottomText: String,
    ): String {
        log.info("GENERATE CHALLENGE ACCEPTED INVOKED => $topText / $bottomText")
        return generateMemeBase64("challenge_accepted.png", topText, bottomText)
    }

    private fun generateMemeBase64(templateName: String, topText: String, bottomText: String): String {
        // Load image from resources/meme folder
        val resourcePath = "/memes/$templateName"
        val imageStream = object {}.javaClass.getResourceAsStream(resourcePath)
            ?: return "Could not generate meme".also {
                log.error("Could not get $templateName image")
            }

        val originalImage = ImageIO.read(imageStream)
        val image = BufferedImage(
            originalImage.width,
            originalImage.height,
            BufferedImage.TYPE_INT_RGB
        )
        val graphics = image.createGraphics()

        // Draw the original image as background
        graphics.drawImage(originalImage, 0, 0, null)

        // Setup antialiasing for smooth text
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Function to calculate optimal font size and draw text
        fun drawMemeText(text: String, isTopText: Boolean) {
            if (text.isBlank()) return

            val maxWidth = originalImage.width - 40 // 20px padding on each side
            val startingFontSize = when {
                originalImage.width < 400 -> 30
                originalImage.width < 600 -> 45
                else -> 60
            }

            var fontSize = startingFontSize
            var font = Font("Impact", Font.BOLD, fontSize)

            // Find optimal font size by checking text width
            do {
                font = Font("Impact", Font.BOLD, fontSize)
                val fontMetrics = graphics.getFontMetrics(font)
                val textWidth = fontMetrics.stringWidth(text)

                if (textWidth <= maxWidth) break
                fontSize -= 2
            } while (fontSize > 20)

            graphics.font = font
            val fontMetrics = graphics.getFontMetrics(font)

            // Handle text wrapping for long text
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val testWidth = fontMetrics.stringWidth(testLine)

                if (testWidth <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        // Single word is too long, add it anyway
                        lines.add(word)
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }

            // Calculate starting Y position
            val lineHeight = fontMetrics.height
            val totalTextHeight = lines.size * lineHeight

            val startY = if (isTopText) {
                maxOf(fontMetrics.ascent + 20, fontMetrics.ascent)
            } else {
                originalImage.height - totalTextHeight - 20 + fontMetrics.ascent
            }

            // Draw each line with outline and fill
            lines.forEachIndexed { index, line ->
                val textWidth = fontMetrics.stringWidth(line)
                val x = (originalImage.width - textWidth) / 2
                val y = startY + (index * lineHeight)

                // Draw black outline (stroke)
                graphics.color = Color.BLACK

                // Draw outline by drawing text slightly offset in all directions
                for (dx in -2..2) {
                    for (dy in -2..2) {
                        if (dx != 0 || dy != 0) {
                            graphics.drawString(line, x + dx, y + dy)
                        }
                    }
                }

                // Draw white fill text
                graphics.color = Color.WHITE
                graphics.drawString(line, x, y)
            }
        }

        // Draw top text
        drawMemeText(topText.uppercase(), true)

        // Draw bottom text
        drawMemeText(bottomText.uppercase(), false)

        graphics.dispose()

        // Convert to Base64
        val outputFile = File("${memeProperties.saveDirectory}\\${UUID.randomUUID()}.png")
        val writeOk = ImageIO.write(image, "png", outputFile)
        if(!writeOk) return "Could not generate and save meme."
        return "Generated meme and saved with name: ${outputFile.name}"
    }
}