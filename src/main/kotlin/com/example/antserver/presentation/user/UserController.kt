package com.example.antserver.presentation.user

import UserNameRequest
import com.example.antserver.util.jwt.JwtTokenManager
import com.example.antserver.application.user.UserService
import org.springframework.web.bind.annotation.*
import com.example.antserver.presentation.user.dto.UserAuthRequest
import com.example.antserver.presentation.user.dto.UserAuthResponse
import com.example.antserver.presentation.user.dto.UserDetailResponse
import com.example.antserver.util.response.CommonResponse
import jakarta.servlet.http.HttpServletRequest
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val jwtTokenManager: JwtTokenManager

) {
    @PostMapping("/auth")
    fun authenticateUser(
        @RequestBody userAuthRequest: UserAuthRequest
    ): CommonResponse<UserAuthResponse> {
        val userAuthResponse = userService.authenticateUser(userAuthRequest)
        return CommonResponse(userAuthResponse)
    }

    @PatchMapping("/name")
    fun updateUserName(
        request: HttpServletRequest,
        @RequestBody userNameRequest: UserNameRequest,
    ): CommonResponse<String> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        userService.updateUser(userId, userNameRequest.name)
        return CommonResponse("${userNameRequest.name}님의 이름이 정상 등록되었습니다.")
    }

    @GetMapping("/self")
    fun getCurrentUser(request: HttpServletRequest): CommonResponse<UserDetailResponse> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val user = userService.findUser(userId)
        return CommonResponse(UserDetailResponse.of(user))
    }
}
