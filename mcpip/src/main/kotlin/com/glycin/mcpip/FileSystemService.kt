package com.glycin.mcpip

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Service
class FileSystemService {

    @Tool(description = "Get the information at a given directory path. Input is the absolute path to the directory.")
    fun getDirectoryInformation(@ToolParam(description = "The absolute path to the directory") input: String): String {
        val absPath = Path.of(input)
        return try {
            val basicAttrs = Files.readAttributes(absPath, BasicFileAttributes::class.java)
            buildString {
                appendLine("Path: ${absPath.pathString}")
                appendLine("Absolute Path: ${absPath.absolutePathString()}")
                appendLine("Exists: ${absPath.exists()}")
                appendLine("Is Directory: ${basicAttrs.isDirectory}")
                appendLine("Is Regular File: ${basicAttrs.isRegularFile}")
                appendLine("Is Symbolic Link: ${basicAttrs.isSymbolicLink}")
                appendLine("Size: ${basicAttrs.size()} bytes")
                appendLine("Creation Time: ${basicAttrs.creationTime()}")
                appendLine("Last Modified: ${basicAttrs.lastModifiedTime()}")
                appendLine("Last Accessed: ${basicAttrs.lastAccessTime()}")

                if (basicAttrs.isDirectory) {
                    val childCount = Files.list(absPath).use { it.count() }
                    appendLine("Child Count: $childCount")
                    val childNames = Files.list(absPath).map {
                        it.fileName.absolutePathString()
                    }
                    appendLine("File names: ${childNames.toList().joinToString()}")
                }
            }
        } catch (e: Exception) {
            "Failed to get info for '$input': ${e.message}"
        }
    }
}