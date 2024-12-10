package com.example.antserver.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.domain.auth.RefreshTokenRepository
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.presentation.dto.GoogleAccessTokenRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.example.antserver.presentation.dto.GoogleAccessTokenResponse
import com.example.antserver.presentation.dto.GoogleUserResponse
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import java.time.Instant
import java.util.*

@Service
class AuthService(
    @Value("\${oauth.google.client-id}") private val clientId: String,
    @Value("\${oauth.google.client-secret}") private val clientSecret: String,
    @Value("\${oauth.google.redirect-uri}") private val redirectUri: String,
    @Value("\${oauth.google.token-url}") private val tokenUrl: String,
    @Value("\${oauth.google.userinfo-url}") private val userInfoUrl: String,
    @Value("\${jwt.secret}") val secretKey: String,
    @Value("\${jwt.expiration-time.access}") val accessTokenExpirationTime: Long,
    @Value("\${jwt.expiration-time.refresh}") val refreshTokenExpirationTime: Long,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val ACCESS_TOKEN_SUBJECT = "AccessToken"
        private const val REFRESH_TOKEN_SUBJECT = "RefreshToken"
        private const val USER_ID_CLAIM = "userId"
    }

    fun isRefreshTokenRevoked(userId: UUID): Boolean {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
        return refreshToken.isRevoked()
    }

    fun authenticateThroughGoogle(authorizationCode: String, provider: ProviderType): User {
        // authorizationCode로 Google JWT Token 요청
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val accessTokenRequestBody = GoogleAccessTokenRequest(
            code = authorizationCode,
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            grantType = "authorization_code",
        ).encode()
        val accessTokenRequest = HttpEntity<String>(accessTokenRequestBody, headers)

        val accessTokenResponse = restTemplate.postForEntity(
            tokenUrl,
            accessTokenRequest,
            GoogleAccessTokenResponse::class.java
        )

        val idToken = accessTokenResponse.body?.idToken
            ?: throw IllegalArgumentException("유효하지 않은 Authorization Code입니다.")

        // idToken(Google JWT Token)으로 유저 정보 요청
        val googleUserResponse = restTemplate.getForEntity(
            userInfoUrl.replace("{idToken}", idToken),
            GoogleUserResponse::class.java
        )

        val googleUser = googleUserResponse.body
            ?: throw IllegalStateException("Google에서 사용자 정보를 가져올 수 없습니다.")

        val email = googleUser.email

        return userRepository.findByEmail(email) ?: userRepository.save(
            User.of(
                name = googleUser.name,
                email = googleUser.email,
                provider = provider,
                providerId = googleUser.sub,
                role = UserRoleType.MEMBER
            )
        )
    }

    fun generateAccessToken(userId: UUID): String {
        val expirationTime = Instant.now().plusMillis(accessTokenExpirationTime)
        return JWT.create()
            .withSubject(ACCESS_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .withClaim(USER_ID_CLAIM, userId.toString())
            .sign(Algorithm.HMAC512(secretKey))
    }

    fun generateRefreshToken(): String {
        val expirationTime = Instant.now().plusMillis(refreshTokenExpirationTime)
        return JWT.create()
            .withSubject(REFRESH_TOKEN_SUBJECT)
            .withExpiresAt(expirationTime)
            .sign(Algorithm.HMAC512(secretKey))
    }

    fun parseClaims(accessToken: String): String? = runCatching {
        JWT.require(Algorithm.HMAC512(secretKey))
            .build()
            .verify(accessToken)
            .getClaim(USER_ID_CLAIM)
            ?.asString()
    }.getOrNull()

    fun getAccessToken(request: HttpServletRequest): String? {
        return request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
    }

    fun getRefreshToken(request: HttpServletRequest): String? {
        return request.cookies
            ?.find { it.name == "RefreshToken" }?.value
    }
}