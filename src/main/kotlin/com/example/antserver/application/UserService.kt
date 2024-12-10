package com.example.antserver.application

import org.springframework.stereotype.Service

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.presentation.dto.user.UserAuthResult
import java.util.*

@Service
class UserService(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {

    // user 인가
    fun authorizeUser(authorizationCode: String, provider: ProviderType): UserAuthResult {
        // 구글 소셜 로그인을 통해 유저 인증
        val user = authService.authenticateThroughGoogle(authorizationCode, provider)

        // token 발급
        val accessToken = authService.generateAccessToken(user.id)
//        val refreshToken = authService.generateRefreshToken()

        return UserAuthResult(accessToken)
    }

    // user 정보 조회
    fun findUser(email: String): User {
        TODO("Not yet implemented")
    }

    // user 정보 수정
    fun updateUser() {
        TODO("Not yet implemented")
    }

    // user 삭제
    fun deleteUser() {
        TODO("Not yet implemented")
    }
}