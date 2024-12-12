package com.example.antserver.application

import com.example.antserver.util.config.JwtProperties
import com.github.f4b6a3.uuid.UuidCreator
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.com.google.common.net.HttpHeaders
import java.util.*

class AuthServiceTest {
    private val authService = AuthService(
        googleOAuthProperties = mockk(),
        jwtProperties = JwtProperties(
            secret = "secret_test",
            expirationTime = JwtProperties.ExpirationTime(
                access = 3600000,
                refresh = 7779600000,
            )
        ),
        userRepository = mockk(),
        refreshTokenRepository = mockk()
    )
    private val userId = UuidCreator.getTimeOrderedEpoch()
    private val mockRequest = mockk<HttpServletRequest>()

    @Test
    @DisplayName("Access Token에서 userId를 추출한다")
    fun testParseClaim() {
        // given
        val accessToken = authService.generateAccessToken(userId)

        // when
        val parsedUserId = UUID.fromString(authService.parseClaims(accessToken))

        // then
        assertThat(parsedUserId).isEqualTo(userId)
    }

    @Test
    @DisplayName("request에서 Access Token을 추출한다")
    fun testGetAccessToken() {
        // given
        val accessToken = authService.generateAccessToken(userId)
        every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $accessToken"

        // when
        val result = authService.getAccessToken(mockRequest)

        // then
        assertThat(result).isEqualTo(accessToken)
    }

    @Test
    @DisplayName("request에서 Refresh Token을 추출한다")
    fun testGetRefreshToken() {
        // given
        val refreshToken = authService.generateRefreshToken()
        val cookie = arrayOf(Cookie("RefreshToken", refreshToken))
        every { mockRequest.cookies } returns cookie

        // when
        val result = authService.getRefreshToken(mockRequest)

        // then
        assertThat(result).isEqualTo(refreshToken)
    }
}