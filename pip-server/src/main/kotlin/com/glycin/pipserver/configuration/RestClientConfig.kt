package com.glycin.pipserver.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun ollamaApi(configurer: RestClientBuilderConfigurer): OllamaApi {
        val objectMapper = ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val restClientBuilder = configurer.configure(RestClient.builder())
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(MappingJackson2HttpMessageConverter(objectMapper))
            }

        return OllamaApi.builder()
            .baseUrl("http://localhost:11434")
            .restClientBuilder(restClientBuilder)
            .build()
    }
}
