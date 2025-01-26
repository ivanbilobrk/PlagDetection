package com.fer.projekt.config

import com.fer.projekt.annotations.SolutionProvider
import com.fer.projekt.controller.SolutionProviderName
import com.fer.projekt.solutionproviders.RepositorySolutionProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SolutionProviderConfig(
    private val solutionProviders: List<RepositorySolutionProvider>
) {

    @Bean
    fun solutionProvidersMap(): Map<SolutionProviderName, RepositorySolutionProvider> {
        return solutionProviders.associateBy { solutionProvider ->
            val annotation = (solutionProvider::class.java.annotations).find { it is SolutionProvider } as SolutionProvider
            annotation.value
        }
    }
}