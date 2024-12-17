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

    fun isTokenValid(token: String): Boolean = runCatching {
        val algorithm = Algorithm.HMAC512(jwtProperties.secret)
        JWT.require(algorithm).build().verify(token)
        true
    }.getOrElse { false }

    fun createAccessToken(userId: UUID): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.access)
        return JWT.create()
            .withSubject(ACCESS_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .withClaim(USER_ID_CLAIM, userId.toString())
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun createRefreshToken(): String {
        val expirationTime = Instant.now().plusMillis(jwtProperties.expirationTime.refresh)
        return JWT.create()
            .withSubject(REFRESH_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .sign(Algorithm.HMAC512(jwtProperties.secret))
    }

    fun renewAccessToken(userId: UUID, refreshToken: String): String {
        assert(isTokenValid(refreshToken)) {
            throw AuthenticationException("Refresh Token이 만료되었습니다. 다시 로그인을 진행해주세요")
        }
        refreshTokenRepository.findByToken(refreshToken)
            ?: throw AuthenticationException("존재하지 않는 Refresh Token입니다.")
        return createAccessToken(userId)
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

    fun parseClaimsWithoutVerify(accessToken: String): String {
        return JWT.decode(accessToken)
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
}