package com.example.antserver.application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import com.example.antserver.util.config.JwtProperties
import com.example.antserver.util.exception.AuthenticationException
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val ACCESS_TOKEN_SUBJECT = "AccessToken"
        private const val REFRESH_TOKEN_SUBJECT = "RefreshToken"
        private const val USER_ID_CLAIM = "userId"
    }

    fun generateAccessToken(userId: UUID): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.access)
        return JWT.create()
            .withSubject(ACCESS_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .withClaim(USER_ID_CLAIM, userId.toString())
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun generateRefreshToken(): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.refresh)
        return JWT.create()
            .withSubject(REFRESH_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun renewRefreshToken(userId: UUID, newRefreshToken: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            ?.apply { update(newRefreshToken) }
            ?: RefreshToken.of(userId, newRefreshToken)
            refreshTokenRepository.save(refreshToken)
    }

    fun parseClaims(accessToken: String): String {
        return JWT.require(Algorithm.HMAC512(jwtProperties.secret))
            .build()
            .verify(accessToken)
            .getClaim(USER_ID_CLAIM)
            ?.asString()
            ?: throw AuthenticationException("Access Token에서 userId 클레임을 추출할 수 없습니다.")
    }

    fun getAccessToken(request: HttpServletRequest): String {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?: throw AuthenticationException("Authorization 헤더가 없습니다.")
        return header.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
            ?: throw AuthenticationException("Bearer <token> 형식이 맞지 않습니다.")
    }

    fun getRefreshToken(request: HttpServletRequest): String {
        return request.cookies
            ?.find { it.name == REFRESH_TOKEN_SUBJECT }?.value
            ?: throw AuthenticationException("쿠키에서 Refresh Token이 누락되었습니다.")
    }
}