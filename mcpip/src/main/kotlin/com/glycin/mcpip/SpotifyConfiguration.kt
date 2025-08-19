package com.glycin.mcpip

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SpotifyProperties::class)
class SpotifyConfiguration

@ConfigurationProperties(prefix = "spotify")
data class SpotifyProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
    val refreshToken: String,
)