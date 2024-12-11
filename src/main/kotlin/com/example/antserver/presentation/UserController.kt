package com.example.antserver.presentation

import com.example.antserver.application.UserService
import org.springframework.web.bind.annotation.*
import com.example.antserver.presentation.dto.user.UserAuthRequest
import com.example.antserver.presentation.dto.user.UserAuthResponse
import com.example.antserver.util.response.CommonResponse

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService

) {
    /*
    유저 인증
     1. Google 소셜 로그인을 통한 유저 인증
     2. 기존 유저가 아니라면 신규 등록
     3. JWT로 Access Token & Refresh Token 발급
    */
    @PostMapping("/auth")
    fun authorizeUser(
        @RequestBody userAuthRequest: UserAuthRequest
    ): CommonResponse<UserAuthResponse> {
        val userAuthResponse = userService.authenticateUser(userAuthRequest)
        return CommonResponse.success(userAuthResponse)
    }

    @PatchMapping("/name")
    fun updateUserName() {
        TODO("Not yet implemented")
    }

    // user 정보 조회
    @GetMapping("/self")
    fun findUser(userEmail: String) {
        TODO("Not yet implemented")
    }

    // user 정보 수정
    fun updateUser() {
        TODO("Not yet implemented")
    }

    // user 삭제 - ADMIN
    fun deleteUser() {
        TODO("Not yet implemented")
    }
}