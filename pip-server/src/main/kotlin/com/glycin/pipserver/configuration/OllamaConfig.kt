package com.glycin.pipserver.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OllamaConfig(
    private val ollama: OllamaChatModel,
    private val chatMemory: ChatMemory,
) {

    @Bean
    fun pipClient() = ChatClient.builder(ollama)
        .defaultAdvisors(
            SimpleLoggerAdvisor(),
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
        )
        .build()
}