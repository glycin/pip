package com.glycin.pipserver.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OllamaConfig(
    private val ollama: OllamaChatModel,
    private val chatMemory: ChatMemory,
    private val toolCallbackProvider: SyncMcpToolCallbackProvider,
) {

    private val noThinkOptions = OllamaChatOptions.builder().disableThinking().build()

    @Bean("pip")
    fun pipClient(): ChatClient {
        return ChatClient.builder(ollama)
            .defaultOptions(noThinkOptions)
            .defaultToolCallbacks(toolCallbackProvider)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .build()
    }

    @Bean("pip_coder")
    fun pipCodingClient(): ChatClient {
        val tools = toolCallbackProvider.toolCallbacks.asSequence().filter {
            !it.toolDefinition.name().startsWith("spring_ai_mcp_client_mcpip_play")
        }.filter {
            !it.toolDefinition.name().startsWith("spring_ai_mcp_client_mcpip_meme")
        }.filter {
            !it.toolDefinition.name().startsWith("spring_ai_mcp_client_mcpip_conference")
        }.toList()

        return ChatClient.builder(ollama)
            .defaultOptions(noThinkOptions)
            .defaultToolCallbacks(tools)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .build()
    }

    @Bean("pip_toolless")
    fun pipToollessClient(): ChatClient {
        return ChatClient.builder(ollama)
            .defaultOptions(noThinkOptions)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .build()
    }
}