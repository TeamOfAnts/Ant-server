package com.example.antserver.application

import com.example.antserver.application.auth.TokenService
import com.example.antserver.util.jwt.JwtTokenManager
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
import org.testcontainers.shaded.com.google.common.net.HttpHeaders
import java.util.*

@SpringBootTest
//@DataJpaTest
//@Import(TestConfiguration::class) TODO 테스트용 필요한 빈만 넣어두는 TestConfiguration 추가
class TokenTest {
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired
    private lateinit var jwtTokenManager: JwtTokenManager
    @Autowired
    private lateinit var tokenService: TokenService
    private val userId = Generators.timeBasedEpochGenerator().generate()
    private val mockRequest = mockk<HttpServletRequest>()

    @Test
    @DisplayName("Access Token에서 userId를 추출한다")
    fun parseClaim() {
        // given
        val accessToken = tokenService.createAccessToken(userId)

        // when
        val parsedUserId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))

        // then
        assertThat(parsedUserId).isEqualTo(userId)
    }

    @Test
    @DisplayName("request에서 Access Token을 추출한다")
    fun getAccessToken() {
        // given
        val accessToken = tokenService.createAccessToken(userId)
        every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $accessToken"

        // when
        val result = jwtTokenManager.getAccessToken(mockRequest)

        // then
        assertThat(result).isEqualTo(accessToken)
    }

    @Test
    @DisplayName("refresh Token이 유효하면 access Token을 재발급한다")
    fun renewAccessTokenWhenRefreshTokenIsValid() {
        // given
        val userId = UUID.randomUUID()
        val refreshToken = tokenService.createRefreshToken()
        refreshTokenRepository.save(RefreshToken.of(userId, refreshToken))

        // when
        val accessToken = tokenService.refreshAccessToken(userId, refreshToken)

        // then
        val parsedUserId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        assertThat(parsedUserId).isEqualTo(userId)
    }

    @Test
    @DisplayName("token이 유효하면 exception을 throw하지 않는다")
    fun returnTrueIfTokenIsValid() {
        // given
        val userId = UUID.randomUUID()
        val accessToken = tokenService.createAccessToken(userId)

        // when & then
        tokenService.isTokenValid(accessToken)
    }

    @Test
    @DisplayName("access Token이 만료되면 AuthenticationException을 Throw한다")
    fun returnFalseIfTokenIsExpired() {
        // given
        val accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBY2Nlc3NUb2tlbiIsImV4cCI6MTczNTI5MjQ3NywidXNlcklkIjoiMDE5NDA3N2QtZTEyNi03ZWY1LWEyYWMtZGZjNWZhOGRmMjU5In0.nbyzUoyxqhARjLE_YEIY9_mMw_iWbCqHrC_Rw3OtJ3dKNXW5xyc7aQrtdxXOjHG9Tcm2CpIZKZALQ32abOYrdQ"

        // when & then
        assertThrows<AuthenticationException> {
            tokenService.isTokenValid(accessToken)
        }
    }
}