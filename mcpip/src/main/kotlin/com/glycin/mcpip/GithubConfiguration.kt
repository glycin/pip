package com.glycin.mcpip

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GithubProperties::class)
class GithubConfiguration

@ConfigurationProperties(prefix = "github")
data class GithubProperties(
    val repositoryPath: String,
    val accessToken: String,
)