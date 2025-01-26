package com.example.antserver.testconfig

import com.example.antserver.fake.*
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestConfig {
    @Primary
    @Bean
    fun fakeUserRepository() = FakeUserRepository()

    @Primary
    @Bean
    fun refreshTokenRepository() = FakeRefreshTokenRepository()

    @Primary
    @Bean
    fun fakePollRepository() = FakePollRepository()

    @Primary
    @Bean
    fun fakeScheduleRepository() = FakeScheduleRepository()

    @Primary
    @Bean
    fun fakeParticipationRepository() = FakeParticipationRepository()
}