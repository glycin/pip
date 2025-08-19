package com.glycin.mcpip

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun fileSystemTools(fileSystemService: FileSystemService): ToolCallbackProvider {
        return MethodToolCallbackProvider
            .builder()
            .toolObjects(fileSystemService)
            .build();
    }

    @Bean
    fun spotifyTools(spotifyService: SpotifyService): ToolCallbackProvider {
        return MethodToolCallbackProvider
            .builder()
            .toolObjects(spotifyService)
            .build()
    }
}