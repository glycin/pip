package com.glycin.pipscripts.slack

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SlackParser {
    private val objectMapper = ObjectMapper().registerKotlinModule().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")

    fun parse(folder: File, outputFolder: File) {
        val files = folder.listFiles { file -> file.isDirectory }
        if(files.isNullOrEmpty()) {
            println("Folder ${folder.name} has no subfolder with a slack dump")
        }

        val dumpFolder = files!![0]
        val jsonFiles = dumpFolder.listFiles { file -> file.isFile}
        if(jsonFiles.isNullOrEmpty()) {
            println("Folder ${dumpFolder.name} has no json files with slack json files")
        }

        println("Found ${jsonFiles!!.size} in folder ${dumpFolder.name}")
        val outPutFile = File("$outputFolder\\msg-history.json")
        val trimmedMessages = mutableListOf<SlackTrimmedMessage>()
        jsonFiles.forEach { jFile ->
            println("Parsing slack dump json: ${jFile.name}")
            val jsonText = jFile.readText()
            val messages = objectMapper.readValue<List<SlackMessage>>(jsonText)
            messages.forEach { m ->
                val millis = Date((m.ts.toDouble() * 1000).toLong())
                val formatted = formatter.format(millis)
                m.text?.let {
                    trimmedMessages.add(SlackTrimmedMessage(UUID.randomUUID().toString(), formatted, m.userProfile.firstName, it))
                }
            }
        }

        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(outPutFile, trimmedMessages)
    }

    fun read(file: File): List<SlackTrimmedMessage> {
        return objectMapper.readValue<List<SlackTrimmedMessage>>(file.readText())
    }
}