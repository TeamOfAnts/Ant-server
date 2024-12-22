package com.example.antserver.util.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import com.example.antserver.util.config.JwtProperties
import com.example.antserver.util.exception.AuthenticationException
import com.example.antserver.util.log.logger
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class JwtTokenManager(
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private val logger = JwtTokenManager::class.logger()

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
            logger.warn("The Refresh Token($refreshToken) for the user with userId($userId) has expired.")
            throw AuthenticationException("인증 오류입니다.")
        }
        refreshTokenRepository.findByToken(refreshToken)
            ?: run {
                logger.warn("The Refresh Token($refreshToken) does not exist.")
                throw AuthenticationException("인증 오류입니다.")
            }
        return createAccessToken(userId)
    }

    fun refreshRefreshToken(userId: UUID, newRefreshToken: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            ?.apply { update(newRefreshToken) }
            ?: RefreshToken.of(userId, newRefreshToken)
        refreshTokenRepository.save(refreshToken)
    }

    fun parseClaims(accessToken: String): String {
        return JWT.require(Algorithm.HMAC512(jwtProperties.secret))
            .build()
            .verify(accessToken)
            .getClaim(jwtProperties.claim)
            ?.asString()
            ?: run {
                logger.warn("Unable to parse the userId from the Access Token.")
                throw AuthenticationException("인증 오류입니다.")
            }
    }

    fun parseClaimsWithoutVerify(accessToken: String): String {
        return JWT.decode(accessToken)
            .getClaim(jwtProperties.claim)
            ?.asString()
            ?: run {
                logger.warn("Unable to parse the userId from the Access Token.")
                throw AuthenticationException("인증 오류입니다.")
            }
    }

    fun getAccessToken(request: HttpServletRequest): String {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Authorization header is missing")
                throw AuthenticationException("인증 오류입니다.")
            }
        return header.takeIf { it.startsWith(jwtProperties.bearerPrefix) }
            ?.removePrefix(jwtProperties.bearerPrefix)
            ?.trim()
            ?: run {
                logger.warn("The format does not match Bearer <token>.")
                throw AuthenticationException("인증 오류입니다.")
            }
    }
}