package com.glycin.pipscripts

import com.glycin.pipscripts.slack.SlackParser
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File

@ShellComponent
class PipCommands {

    @ShellMethod(key = ["slack"])
    fun parseSlack(
        @ShellOption(help = "Path to slack dump folder") folder: File,
        @ShellOption(help = "Path to output slack dump to") outputFolder: File
    ): String {
        return if(folder.isDirectory) {
            SlackParser(folder, outputFolder)
                .parse()
            "Parsed slack messages from folder: ${folder.absolutePath} and exported them to:"
        } else {
            "Path is not a directory (${folder.absolutePath}"
        }
    }
}