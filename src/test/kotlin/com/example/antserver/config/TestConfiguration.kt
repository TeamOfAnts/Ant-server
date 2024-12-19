package com.example.antserver.config

import com.example.antserver.application.auth.TokenService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfiguration {
    @Bean
    fun authService(): TokenService {
        return authService()
    }
}