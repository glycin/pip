package com.glycin.mcpip

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(MemeProperties::class)
class MemeConfiguration

@ConfigurationProperties(prefix = "meme")
data class MemeProperties(
    val saveDirectory: String,
)