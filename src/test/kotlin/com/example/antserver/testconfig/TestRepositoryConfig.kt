package com.example.antserver.testconfig

import com.example.antserver.fake.*
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestRepositoryConfig {
    @Bean
    @Primary
    fun fakeUserRepository() = FakeUserRepository()

    @Bean
    @Primary
    fun refreshTokenRepository() = FakeRefreshTokenRepository()

    @Bean
    @Primary
    fun fakePollRepository() = FakePollRepository()

    @Bean
    @Primary
    fun fakeScheduleRepository() = FakeScheduleRepository()

    @Bean
    @Primary
    fun fakeParticipationRepository() = FakeParticipationRepository()
}