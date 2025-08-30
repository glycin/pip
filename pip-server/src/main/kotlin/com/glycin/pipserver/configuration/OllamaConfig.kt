package com.glycin.pipserver.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OllamaConfig(
    private val ollama: OllamaChatModel,
    private val chatMemory: ChatMemory,
    private val toolCallbackProvider: SyncMcpToolCallbackProvider,
) {

    @Bean("pip")
    fun pipClient(): ChatClient {
        return ChatClient.builder(ollama)
            .defaultToolCallbacks(toolCallbackProvider)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .build()
    }

    @Bean("pip_coder")
    fun pipCodingClient(): ChatClient {
        val tools = toolCallbackProvider.toolCallbacks.filter {
            !it.toolDefinition.name().startsWith("spring_ai_mcp_client_mcpip_play")
        }.filter {
            !it.toolDefinition.name().startsWith("spring_ai_mcp_client_mcpip_meme")
        }
        return ChatClient.builder(ollama)
            .defaultToolCallbacks(tools)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .build()
    }
}