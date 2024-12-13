package com.example.antserver.presentation.user

import com.example.antserver.application.auth.AuthService
import com.example.antserver.application.user.UserService
import org.springframework.web.bind.annotation.*
import com.example.antserver.presentation.user.dto.UserAuthRequest
import com.example.antserver.presentation.user.dto.UserAuthResponse
import com.example.antserver.presentation.user.dto.UserDetailResponse
import com.example.antserver.util.response.CommonResponse
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val authService: AuthService

) {
    @PostMapping("/auth")
    fun authenticateUser(
        @RequestBody userAuthRequest: UserAuthRequest
    ): CommonResponse<UserAuthResponse> {
        val userAuthResponse = userService.authenticateUser(userAuthRequest)
        return CommonResponse(userAuthResponse)
    }

    @PatchMapping("/name/{name}")
    fun updateUserName(
        @RequestHeader("Authorization") accessToken: String,
        @PathVariable name: String,
    ): CommonResponse<String> {
        val userId = UUID.fromString(authService.parseClaims(accessToken))
        userService.updateUser(userId, name)
        return CommonResponse("${name}님의 이름이 정상 등록되었습니다.")
    }

    @GetMapping("/self")
    fun getCurrentUser(@RequestHeader("Authorization") accessToken: String): CommonResponse<UserDetailResponse> {
        val userId = UUID.fromString(authService.parseClaims(accessToken))
        val user = userService.findUser(userId)
        return CommonResponse(UserDetailResponse.of(user))
    }
}