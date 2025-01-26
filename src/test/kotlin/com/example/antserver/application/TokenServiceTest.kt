package com.example.antserver.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.application.auth.TokenService
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.fake.FakeRefreshTokenRepository
import com.example.antserver.testconfig.TestConfig
import com.example.antserver.util.exception.AuthenticationException
import com.example.antserver.util.security.jwt.JwtProperties
import com.example.antserver.util.security.jwt.JwtTokenManager
import com.fasterxml.uuid.Generators
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.testcontainers.shaded.com.google.common.net.HttpHeaders
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
class TokenServiceTest {
    @Autowired
    private lateinit var fakeRefreshTokenRepository: FakeRefreshTokenRepository

    @Autowired
    private lateinit var jwtTokenManager: JwtTokenManager

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var jwtProperties: JwtProperties

    private val userId = Generators.timeBasedEpochGenerator().generate()
    private val mockRequest = mockk<HttpServletRequest>()
//
//    @Test
//    @DisplayName("Access Token에서 userId를 추출한다")
//    fun parseClaim() {
//        // given
//        val accessToken = tokenService.createAccessToken(userId)
//
//        // when
//        val parsedUserId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
//
//        // then
//        assertThat(parsedUserId).isEqualTo(userId)
//    }
//
//    @Test
//    @DisplayName("request에서 Access Token을 추출한다")
//    fun getAccessToken() {
//        // given
//        val accessToken = tokenService.createAccessToken(userId)
//        every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $accessToken"
//
//        // when
//        val result = jwtTokenManager.getAccessToken(mockRequest)
//
//        // then
//        assertThat(result).isEqualTo(accessToken)
//    }
//
//    @Test
//    @DisplayName("refresh Token이 유효하면 access Token을 재발급한다")
//    fun renewAccessTokenWhenRefreshTokenIsValid() {
//        // given
//        val refreshToken = tokenService.createRefreshToken()
//        fakeRefreshTokenRepository.save(RefreshToken.of(userId, refreshToken))
//
//        // when
//        val accessToken = tokenService.refreshAccessToken(userId, refreshToken)
//
//        // then
//        val parsedUserId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
//        assertThat(parsedUserId).isEqualTo(userId)
//    }
//
//    @Test
//    @DisplayName("token이 유효하면 exception을 throw하지 않는다")
//    fun returnTrueIfTokenIsValid() {
//        // given
//        val accessToken = tokenService.createAccessToken(userId)
//
//        // when & then
//        tokenService.isTokenValid(accessToken)
//    }
//
//    @Test
//    @DisplayName("access Token이 만료되면 AuthenticationException을 Throw한다")
//    fun returnFalseIfTokenIsExpired() {
//        // given
//        val accessToken = JWT.create()
//            .withSubject("AccessToken")
//            .withExpiresAt(Date(System.currentTimeMillis() - 1000))
//            .withClaim("userId", userId.toString())
//            .sign(Algorithm.HMAC512(jwtProperties.secret))
//
//        // then
//        assertThrows<AuthenticationException> {
//            tokenService.isTokenValid(accessToken)
//        }
//    }
}