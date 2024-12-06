package com.example.antserver.domain.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.user.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.example.antserver.presentation.dto.GoogleAccessTokenResponse
import com.example.antserver.presentation.dto.GoogleUserResponse
import java.time.Instant
import java.util.*


@Service
class AuthService(
    @Value("\${authorize.client-id}") private val clientId: String,
    @Value("\${authorize.client-secret}") private val clientSecret: String,
    @Value("\${authorize.redirect-uri}") private val redirectUri: String,
    @Value("\${spring.jwt.secret}") val secretKey: String,
    @Value("\${spring.jwt.access.expiration}") val accessTokenExpirationTime: Long,
    @Value("\${spring.jwt.refresh.expiration}") val refreshTokenExpirationTime: Long,
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
        return try {
            // authorizationCode로 Google에 accessToken 요청
            val restTemplate = RestTemplate()
            val googleRequestParam = GoogleAccessTokenRequest(
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = redirectUri,
                grantType = "authorization_code",
                code = authorizationCode
            )

            val accessTokenResponse = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                googleRequestParam,
                GoogleAccessTokenResponse::class.java
            )

            val accessToken = accessTokenResponse.body?.accessToken
                ?: throw IllegalArgumentException("유효하지 않은 Authorization Code입니다.")

            // accessToken으로 유저 정보 요청
            val googleUserInfoResponse = restTemplate.getForEntity(
                "https://oauth2.googleapis.com/tokeninfo?id_token=$accessToken",
                GoogleUserResponse::class.java
            )

            val googleUser = googleUserInfoResponse.body
                ?: throw IllegalStateException("Google에서 사용자 정보를 가져올 수 없습니다.")

            val email = googleUser.email
            var user = userRepository.findByEmail(email)

            // 신규 회원이면 유저 정보의 email 저장
            if (user == null) {
                user = User.of(
                    email = googleUser.email,
                    provider = provider,
                    role = UserRoleType.MEMBER
                )
                user = userRepository.save(user)
            }
            user
        } catch (e: Exception) {
            throw IllegalStateException("인증 실패: ${e.message}")
        }
    }

    fun setTokenToHeader() {
        TODO()
        // Filter에 구현되어있는데 어떻게 가져와야하지?
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

    fun setAccessTokenToHeader(accessToken: String): HttpHeaders {
        // Access Token을 Authorization 헤더에 설정
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }
        return headers
    }

    fun setAccessAndRefreshTokenToHeader(accessToken: String, refreshToken: String): HttpHeaders {
        // Access Token을 Authorization 헤더에 설정
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")

            // Refresh Token을 HTTP Only 쿠키에 설정
            val refreshTokenCookie = ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true) // HTTP Only 설정
                .secure(false) // TODO. HTTPS 처리 후 true로 변경
                .path("/")
                .maxAge(refreshTokenExpirationTime)
                .sameSite("None") // Cross-Site 요청 허용
                .build()

            add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        }
        return headers
    }

    fun parseClaims(accessToken: String): String? = runCatching {
        JWT.require(Algorithm.HMAC512(secretKey))
            .build()
            .verify(accessToken)
            .getClaim(USER_ID_CLAIM)
            ?.asString()
    }.getOrNull()

    fun extractAccessToken(request: HttpServletRequest): String? {
        return request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
    }

    fun extractRefreshToken(request: HttpServletRequest): String? {
        return request.cookies
            ?.find { it.name == "RefreshToken" }?.value
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}