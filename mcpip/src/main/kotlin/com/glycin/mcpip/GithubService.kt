package com.glycin.mcpip

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Service
class GithubService(
    private val githubProperties: GithubProperties,
) {
    private val log = LoggerFactory.getLogger(SpotifyService::class.java)

    @Tool(description = "Creates a new Git branch in a local repository")
    fun createBranch(
        @ToolParam(description = "New branch name to create. For example: my-awesome-branch") branchName: String,
    ): String {
        log.info("Invoked git create branch tool with branch name ${branchName}!")
        return try {
            val outputs = mutableListOf<String>()

            // Checkout the base branch first
            executeGitCommand(githubProperties.repositoryPath, "git", "checkout", "main")

            // Create and checkout new branch
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "checkout", "-b", branchName))
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "push", "-u", "origin", branchName))
            "Branch '$branchName' created successfully from main"
        } catch (e: Exception) {
            "Failed to create branch: ${e.message}"
        }
    }

    @Tool(description = "Commits changes and pushes them to GitHub with a commit message")
    fun commitAndPush(
        @ToolParam(description = "The commit message for these changes.") commitMessage: String,
    ): String {
        log.info("Invoked git commit with commit message ${commitMessage}!")

        return try {
            val outputs = mutableListOf<String>()

            // Add files
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "add", "."))

            // Commit changes
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "commit", "-m", commitMessage))

            // Push to remote
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "push"))

            "Successfully committed and pushed to remote, with the following outputs: ${outputs.joinToString("\n")}"
        } catch (e: Exception) {
            "Failed to push: ${e.message}"
        }
    }
    private fun executeGitCommand(workingDir: String, vararg command: String): String {
        val processBuilder = ProcessBuilder(*command)
            .directory(File(workingDir))
            .redirectErrorStream(true)

        val process = processBuilder.start()
        val output = BufferedReader(InputStreamReader(process.inputStream))
            .readText()

        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("Command failed with exit code $exitCode: $output")
        }

        return output
    }
}