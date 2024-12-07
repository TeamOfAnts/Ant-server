package com.example.antserver.application

import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {

    // 로그인
    fun authenticateUser(authorizationCode: String, provider: ProviderType): UserAuthResult {
        // 구글 소셜 로그인을 통해 유저 인증
        val user = authService.authenticateThroughGoogle(authorizationCode, provider)

        // 헤더 세팅
        val header = authService.setTokenToHeader()

        return UserAuthResult(header, user.name, user.email, user.role)
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