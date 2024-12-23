package com.example.antserver.application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import com.example.antserver.util.config.JwtProperties
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties
) {

    fun isTokenValid(token: String): Boolean = runCatching {
        val algorithm = Algorithm.HMAC512(jwtProperties.secret)
        JWT.require(algorithm).build().verify(token)
        true
    }.getOrElse { false }

    fun createAccessToken(userId: UUID): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.access)
        return JWT.create()
            .withSubject(jwtProperties.accessTokenSubject)
            .withExpiresAt(expirationTime)
            .withClaim(jwtProperties.claim, userId.toString())
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun createRefreshToken(): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.refresh)
        return JWT.create()
            .withSubject(jwtProperties.refreshTokenSubject)
            .withExpiresAt(expirationTime)
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun refreshAccessToken(userId: UUID, refreshToken: String): String {
        assert(isTokenValid(refreshToken)) {
            throw ApplicationException(Status.Unauthorized, "The Refresh Token($refreshToken) for the user with userId($userId) has expired.", "인증 오류입니다.")
        }
        refreshTokenRepository.findByToken(refreshToken)
            ?: throw ApplicationException(Status.Unauthorized, "The Refresh Token($refreshToken) does not exist.", "인증 오류입니다.")
        return createAccessToken(userId)
    }

    fun updateRefreshToken(userId: UUID, newRefreshToken: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            ?.apply { update(newRefreshToken) }
            ?: RefreshToken.of(userId, newRefreshToken)
        refreshTokenRepository.save(refreshToken)
    }
}