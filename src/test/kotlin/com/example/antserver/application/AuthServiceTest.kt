package com.example.antserver.application

import com.example.antserver.application.auth.AuthService
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import com.example.antserver.util.exception.AuthenticationException
import com.fasterxml.uuid.Generators
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.testcontainers.shaded.com.google.common.net.HttpHeaders
import java.util.*

@SpringBootTest
//@DataJpaTest
//@Import(TestConfiguration::class)
class AuthServiceTest {
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired
    private lateinit var authService: AuthService
    private val userId = Generators.timeBasedEpochGenerator().generate()
    private val mockRequest = mockk<HttpServletRequest>()

    @Test
    @DisplayName("Access Token에서 userId를 추출한다")
    fun parseClaim() {
        // given
        val accessToken = authService.createAccessToken(userId)

        // when
        val parsedUserId = UUID.fromString(authService.parseClaims(accessToken))

        // then
        assertThat(parsedUserId).isEqualTo(userId)
    }

    @Test
    @DisplayName("request에서 Access Token을 추출한다")
    fun getAccessToken() {
        // given
        val accessToken = authService.createAccessToken(userId)
        every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $accessToken"

        // when
        val result = authService.getAccessToken(mockRequest)

        // then
        assertThat(result).isEqualTo(accessToken)
    }

    @Test
    @DisplayName("refresh Token이 유효하면 access Token을 재발급한다")
    fun renewAccessTokenWhenRefreshTokenIsValid() {
        // given
        val userId = UUID.randomUUID()
        val refreshToken = authService.createRefreshToken()
        refreshTokenRepository.save(RefreshToken.of(userId, refreshToken))

        // when
        val accessToken = authService.renewAccessToken(userId, refreshToken)

        // then
        val parsedUserId = UUID.fromString(authService.parseClaims(accessToken))
        assertThat(parsedUserId).isEqualTo(userId)
    }

    @Test
    @DisplayName("refresh Token이 만료되면 AuthenticationException을 Throw한다")
    fun failToRenewAccessTokenWhenRefreshTokenIsExpired() {
        // given
        val userId = UUID.randomUUID()
        val refreshToken = authService.createRefreshToken()

        // when & then
        assertThrows<AuthenticationException> {
            authService.renewAccessToken(userId, refreshToken)
        }
    }

    @Test
    @DisplayName("token이 유효하면 true를 반환한다")
    fun returnTrueIfTokenIsValid() {
        // given
        val userId = UUID.randomUUID()
        val accessToken = authService.createAccessToken(userId)

        // when
        val isValid = authService.isTokenValid(accessToken)

        // then
        assertThat(isValid).isTrue()
    }

    @Test
    @DisplayName("token이 만료되면 false를 반환한다")
    fun returnFalseIfTokenIsExpired() {
        // given
        val accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBY2Nlc3NUb2tlbiIsImV4cCI6MTczNDExNzk1NiwidXNlcklkIjoiMDE5M2MxNDUtMmRmNy03MzE4LTk3ZTItZTQzYWQ4M2FkMzNjIn0.ya2CDWDq40aJQNSS2nN8K6328-M5baNB7IFEZXxNlwznXXFmypFGlsRpaar_n5CPtwNoKue3Hc7NteH3Xf2udw"

        // when
        val isValid = authService.isTokenValid(accessToken)

        // then
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("만료된 Access Token에서 userId를 추출한다")
    fun parseClaimWithoutVerify() {
        // given
        val userId = "0193c145-2df7-7318-97e2-e43ad83ad33c"
        val accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBY2Nlc3NUb2tlbiIsImV4cCI6MTczNDExNzk1NiwidXNlcklkIjoiMDE5M2MxNDUtMmRmNy03MzE4LTk3ZTItZTQzYWQ4M2FkMzNjIn0.ya2CDWDq40aJQNSS2nN8K6328-M5baNB7IFEZXxNlwznXXFmypFGlsRpaar_n5CPtwNoKue3Hc7NteH3Xf2udw"

        // when
        val parsedUserId = authService.parseClaimsWithoutVerify(accessToken)

        // then
        assertThat(parsedUserId).isEqualTo(userId)
    }
}