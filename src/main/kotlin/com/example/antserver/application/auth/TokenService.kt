package com.example.antserver.application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.TokenExpiredException
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

    fun isTokenValid(token: String) {
        try {
            val algorithm = Algorithm.HMAC512(jwtProperties.secret)
            JWT.require(algorithm).build().verify(token)
        } catch (exception: TokenExpiredException) {
            throw ApplicationException(Status.Unauthorized, "The token has expired.", "Access token이 만료되었습니다.")
        } catch (exception: Exception) {
            throw ApplicationException(Status.BadRequest, exception.message, "인증 오류입니다.")
        }
    }

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
        isTokenValid(refreshToken)
        findByToken(refreshToken)
        return createAccessToken(userId)
    }

    fun updateRefreshToken(userId: UUID, newRefreshToken: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            ?.apply { update(newRefreshToken) }
            ?: RefreshToken.of(userId, newRefreshToken)
        refreshTokenRepository.save(refreshToken)
    }

    fun findByToken(token: String): RefreshToken {
        return refreshTokenRepository.findByToken(token)
            ?: throw ApplicationException(Status.BadRequest, "The Refresh Token $token does not exist.", "인증 오류입니다.")
    }
}