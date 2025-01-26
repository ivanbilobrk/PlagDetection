package com.fer.projekt.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class JPlagExecutorServiceConfig {

    @Bean("newVirtualThreadPerTaskExecutor")
    fun jPlagExecutorService() = Executors.newVirtualThreadPerTaskExecutor()
}