package com.example.antserver.testconfig

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@TestConfiguration
class TestClockConfig {
    @Bean
    @Primary
    fun testClock(): Clock {
        return Clock.fixed(
            LocalDateTime.of(2025, 2, 6, 18, 0, 0)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant(),
            ZoneId.systemDefault())
    }
}

