package com.example.antserver.application.user

import com.example.antserver.util.jwt.JwtTokenManager
import com.example.antserver.domain.user.ProviderType
import org.springframework.stereotype.Service
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.presentation.user.dto.GoogleAccessTokenResponse
import com.example.antserver.presentation.user.dto.GoogleProfileResponse
import com.example.antserver.presentation.user.dto.UserAuthRequest
import com.example.antserver.presentation.user.dto.UserAuthResponse
import com.example.antserver.util.config.GoogleOAuthProperties
import com.example.antserver.util.exception.AuthenticationException
import com.example.antserver.util.exception.UserNotFoundException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtTokenManager: JwtTokenManager,
    private val googleOAuthProperties: GoogleOAuthProperties,
    ) {

    @Transactional
    fun authenticateUser(userAuthRequest: UserAuthRequest): UserAuthResponse {
        val googleUser = authenticateThroughGoogle(userAuthRequest.authorizationCode)
        val user = authenticateByEmailOrRegister(googleUser, userAuthRequest.provider)

        val accessToken = jwtTokenManager.createAccessToken(user.id)
        val refreshToken = jwtTokenManager.createRefreshToken()

        jwtTokenManager.renewRefreshToken(user.id, refreshToken)

        return UserAuthResponse.of(accessToken, refreshToken)
    }

    fun authenticateThroughGoogle(authorizationCode: String): GoogleProfileResponse {
        val googleAccessToken = getGoogleAccessToken(authorizationCode)
        return getGoogleProfile(googleAccessToken)
    }

    fun getGoogleAccessToken(authorizationCode: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val googleTokenRequestParams = LinkedMultiValueMap<String, String>()
        googleTokenRequestParams.add("code", authorizationCode)
        googleTokenRequestParams.add("client_id", googleOAuthProperties.clientId)
        googleTokenRequestParams.add("client_secret", googleOAuthProperties.clientSecret)
        googleTokenRequestParams.add("redirect_uri", googleOAuthProperties.redirectUri)
        googleTokenRequestParams.add("grant_type", "authorization_code")
        val googleTokenRequestBody = UriComponentsBuilder.newInstance()
            .queryParams(googleTokenRequestParams)
            .build()
            .query
            .orEmpty()

        val googleTokenRequest = HttpEntity<String>(googleTokenRequestBody, headers)

        return RestTemplate().postForEntity(
            googleOAuthProperties.tokenUrl,
            googleTokenRequest,
            GoogleAccessTokenResponse::class.java
        ).body?.idToken
            ?: throw AuthenticationException("유효하지 않은 Authorization Code입니다.")
    }

    fun getGoogleProfile(googleJwtToken: String): GoogleProfileResponse {
        return RestTemplate().getForEntity(
            googleOAuthProperties.userInfoUrl.replace("{idToken}", googleJwtToken),
            GoogleProfileResponse::class.java
        ).body?.takeIf { it.emailVerified }
            ?: throw UserNotFoundException("Google에서 유저 정보를 가져올 수 없습니다.")
    }

    fun authenticateByEmailOrRegister(googleUser: GoogleProfileResponse, provider: ProviderType): User {
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

    @Transactional
    fun updateUser(userId: UUID, newName: String): User {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("유저를 찾을 수 없습니다.")
        user.updateName(newName)
        return userRepository.save(user)
    }

    fun findUser(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw UserNotFoundException("유저를 찾을 수 없습니다.")
    }
}