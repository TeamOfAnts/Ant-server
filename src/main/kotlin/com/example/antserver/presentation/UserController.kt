package com.example.antserver.presentation

import com.example.antserver.application.UserService
import org.springframework.web.bind.annotation.*
import com.example.antserver.presentation.dto.user.UserAuthRequest
import com.example.antserver.presentation.dto.user.UserAuthResult
import com.example.antserver.util.response.CommonResponse

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService

) {
    /*
    소셜 로그인
     1. Google 소셜 로그인
     2. JWT로 Access Token & Refresh Token 발급
    */
    @PostMapping("/auth")
    fun authorizeUser(
        @RequestBody userAuthRequest: UserAuthRequest
    ): CommonResponse<UserAuthResult> {
        println("Received Request: $userAuthRequest")
        val (authorizationCode, provider ) = userAuthRequest
        val userAuthResult = userService.authorizeUser(authorizationCode, provider)
        return CommonResponse.success(userAuthResult)
    }

    @PostMapping("/name")
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