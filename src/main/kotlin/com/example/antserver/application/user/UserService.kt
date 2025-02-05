package com.example.antserver.application.user

import com.example.antserver.application.auth.TokenService
import com.example.antserver.domain.user.ProviderType
import org.springframework.stereotype.Service
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.presentation.user.dto.UserAuthRequest
import com.example.antserver.presentation.user.dto.UserAuthResponse
import com.example.antserver.presentation.user.dto.oauth.ProfileResponse
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.oauth.OAuthClientFactory
import com.example.antserver.util.response.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val oAuthClientFactory: OAuthClientFactory
    ) {

    @Transactional
    suspend fun authenticateUser(userAuthRequest: UserAuthRequest): UserAuthResponse = coroutineScope {
        val socialUser = oAuth(userAuthRequest.authorizationCode, userAuthRequest.provider)
        val (userId, isNew) = authenticateOrRegister(socialUser, userAuthRequest.provider)

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

    fun oAuth(authorizationCode: String, provider: ProviderType): ProfileResponse {
        val oAuthClient = oAuthClientFactory.getClient(provider)
        val oAuthAccessToken = oAuthClient.getAccessToken(authorizationCode)
        return oAuthClient.getUserProfile(oAuthAccessToken)
    }

    fun authenticateOrRegister(socialUser: ProfileResponse, provider: ProviderType): Pair<UUID, Boolean> {
        val email = socialUser.email
        val existingUser = userRepository.findByEmailAndProvider(email, provider)

        return if (existingUser != null) {
            Pair(existingUser.id, false)
        } else {
            val newUser = userRepository.save(
                User.of(
                    name = socialUser.name,
                    email = socialUser.email,
                    provider = provider,
                    providerId = socialUser.providerId,
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