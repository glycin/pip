package com.glycin.pipserver.coder

import com.glycin.pipserver.util.NanoId
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class CoderService(
    ollama: OllamaChatModel,
    chatMemory: ChatMemory,
) {
    private val pipCoder = ChatClient.builder(ollama)
        .defaultAdvisors(
            SimpleLoggerAdvisor(),
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
        )
        .build()

    fun generate(codingRequestBody: CodingRequestBody): String? {
        return with(codingRequestBody) {
            pipCoder
                .prompt(Prompt(input))
                .system("You are a very sarcastic software engineer ${if(think)"/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId ?: NanoId.generate()) }
                .call()
                .content()
        }
    }

    fun generateStream(codingRequestBody: CodingRequestBody): Flux<String>? {
        return with(codingRequestBody) {
            pipCoder
                .prompt(Prompt(input))
                .system("You are a very sarcastic software engineer ${if (think) "/think" else "/no_think"}")
                .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId ?: NanoId.generate()) }
                .stream()
                .content()
        }
    }
}