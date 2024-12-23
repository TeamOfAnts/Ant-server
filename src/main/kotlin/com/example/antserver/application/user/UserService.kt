package com.example.antserver.application.user

import com.example.antserver.application.auth.TokenService
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
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val googleOAuthProperties: GoogleOAuthProperties,
    private val tokenService: TokenService,
    ) {

    @Transactional
    suspend fun authenticateUser(userAuthRequest: UserAuthRequest): UserAuthResponse = coroutineScope {
        val googleUser = authenticateThroughGoogle(userAuthRequest.authorizationCode)
        val (userId, isNew) = authenticateByEmailOrRegister(googleUser, userAuthRequest.provider)

        val deferredNewRefreshToken = async {
            tokenService.createRefreshToken()
        }
        val deferredNewAccessToken = async {
            tokenService.createAccessToken(userId)
        }
        val newRefreshToken = deferredNewRefreshToken.await()
        val newAccessToken = deferredNewAccessToken.await()

        tokenService.updateRefreshToken(userId, newRefreshToken)

        return@coroutineScope UserAuthResponse.of(newAccessToken, newRefreshToken, isNew)
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
            ?: throw ApplicationException(Status.Unauthorized, "Invalid Authorization Code ($authorizationCode)", "인증 오류입니다.")
    }

    fun getGoogleProfile(googleJwtToken: String): GoogleProfileResponse {
        return RestTemplate().getForEntity(
            googleOAuthProperties.userInfoUrl.replace("{idToken}", googleJwtToken),
            GoogleProfileResponse::class.java
        ).body?.takeIf { it.emailVerified }
            ?: throw ApplicationException(Status.Unauthorized, "Can't get google profile from ${googleOAuthProperties.userInfoUrl} with $googleJwtToken", "인증 오류입니다.")
    }

    fun authenticateByEmailOrRegister(googleUser: GoogleProfileResponse, provider: ProviderType): Pair<UUID, Boolean> {
        val email = googleUser.email
        val existingUser = userRepository.findByEmail(email)

        return if (existingUser != null) {
            Pair(existingUser.id, false)
        } else {
            val newUser = userRepository.save(
                User.of(
                    name = googleUser.name,
                    email = googleUser.email,
                    provider = provider,
                    providerId = googleUser.sub,
                    role = UserRoleType.MEMBER
                )
            )
            Pair(newUser.id, true)
        }
    }

    @Transactional
    fun updateUser(userId: UUID, newName: String) {
        val user = findUser(userId)
        user.updateName(newName)
        userRepository.save(user)
    }

    fun findUser(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw ApplicationException(Status.NotFound, "User not exists with id: $userId", "유저를 찾을 수 없습니다.")
    }
}