package com.example.antserver.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.domain.user.UserRoleType
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.example.antserver.presentation.dto.user.GoogleTokenResponse
import com.example.antserver.presentation.dto.user.GoogleUserResponse
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder
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

    fun authenticateThroughGoogle(authorizationCode: String): GoogleUserResponse {
        val googleJwtToken = fetchGoogleJwtToken(authorizationCode)
        return fetchGoogleUser(googleJwtToken)
    }

    fun fetchGoogleJwtToken(authorizationCode: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val googleTokenRequestParams = LinkedMultiValueMap<String, String>()
        googleTokenRequestParams.add("code", authorizationCode)
        googleTokenRequestParams.add("client_id", clientId)
        googleTokenRequestParams.add("client_secret", clientSecret)
        googleTokenRequestParams.add("redirect_uri", redirectUri)
        googleTokenRequestParams.add("grant_type", "authorization_code")
        val googleTokenRequestBody = UriComponentsBuilder.newInstance()
            .queryParams(googleTokenRequestParams)
            .build()
            .query
            .orEmpty()

        val googleTokenRequest = HttpEntity<String>(googleTokenRequestBody, headers)

        return RestTemplate().postForEntity(
            tokenUrl,
            googleTokenRequest,
            GoogleTokenResponse::class.java
        ).body?.idToken
            ?: throw IllegalArgumentException("유효하지 않은 Authorization Code입니다.")
    }

    fun fetchGoogleUser(googleJwtToken: String): GoogleUserResponse {
        return RestTemplate().getForEntity(
            userInfoUrl.replace("{idToken}", googleJwtToken),
            GoogleUserResponse::class.java
        ).body?.takeIf { it.emailVerified }
            ?: throw IllegalStateException("Google에서 사용자 정보를 가져올 수 없습니다.")
    }

    fun authenticateByEmailOrRegister(googleUser: GoogleUserResponse, provider: ProviderType): User {
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

    fun upsertRefreshToken(userId: UUID, newRefreshToken: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            ?.apply { update(newRefreshToken) }
            ?: RefreshToken.of(userId, newRefreshToken)
            refreshTokenRepository.save(refreshToken)
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