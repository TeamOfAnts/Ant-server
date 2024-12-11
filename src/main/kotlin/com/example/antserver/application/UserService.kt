package com.example.antserver.application

import org.springframework.stereotype.Service
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.presentation.dto.user.UserAuthRequest
import com.example.antserver.presentation.dto.user.UserAuthResponse
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {

    @Transactional
    fun authenticateUser(userAuthRequest: UserAuthRequest): UserAuthResponse {
        // 구글 소셜 로그인을 통해 유저 인증
        val googleUser = authService.authenticateThroughGoogle(userAuthRequest.authorizationCode)
        val user = authService.authenticateByEmailOrRegister(googleUser, userAuthRequest.provider)

        // token 발급
        val accessToken = authService.generateAccessToken(user.id)
        val refreshToken = authService.generateRefreshToken()

        // refresh token 저장
        authService.upsertRefreshToken(user.id, refreshToken)

        return UserAuthResponse.of(accessToken, refreshToken)
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