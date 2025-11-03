package com.glycin.mcpip

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

    @Tool(
        name = "createGitBranch",
        description = "Creates a new Git branch in a local repository"
    )
    fun createBranch(
        @ToolParam(description = "New branch name to create. For example: my-awesome-branch") branchName: String,
    ): GitResult {
        return try {
            // Checkout the base branch first
            executeGitCommand(githubProperties.repositoryPath, "git", "checkout", "main")

            // Create and checkout new branch
            val output = executeGitCommand(githubProperties.repositoryPath, "git", "checkout", "-b", branchName)

            GitResult(
                success = true,
                message = "Branch '$branchName' created successfully from main",
                output = output
            )
        } catch (e: Exception) {
            GitResult(
                success = false,
                message = "Failed to create branch: ${e.message}",
                output = null
            )
        }
    }

    @Tool(
        name = "pushToGitHub",
        description = "Commits changes and pushes them to GitHub with a commit message"
    )
    fun pushToGitHub(
        @ToolParam(description = "The commit message for these changes.") commitMessage: String,
    ): GitResult {
        return try {
            val outputs = mutableListOf<String>()

            // Add files
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "add", "."))

            // Commit changes
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "commit", "-m", commitMessage))

            // Push to remote
            outputs.add(executeGitCommand(githubProperties.repositoryPath, "git", "push"))

            GitResult(
                success = true,
                message = "Successfully committed and pushed to",
                output = outputs.joinToString("\n")
            )
        } catch (e: Exception) {
            GitResult(
                success = false,
                message = "Failed to push: ${e.message}",
                output = null
            )
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

    data class GitResult(
        val success: Boolean,
        val message: String,
        val output: String?
    )
}