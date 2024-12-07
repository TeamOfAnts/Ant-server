package com.example.antserver.presentation

import com.example.antserver.application.UserService
import org.springframework.http.ResponseEntity
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
    소셜 로그인(Refresh Token 만료 시)
     1. Google Access Token 발급 및 사용자 이메일 일치 여부 확인
     2. JWT로 Refresh Token 발급
     3. HttpOnly 헤더에 Refresh Token 저장 후 클라이언트에게 응답
     4. 메인 화면으로 Redirect
    */
    @PostMapping("/auth")
    fun authenticate(
        @RequestBody authRequest: UserAuthRequest
        ): ResponseEntity<CommonResponse<UserAuthResponse>> {
            val authorizationCode = authRequest.authorizationCode
            val provider = authRequest.provider

            val authResult = userService.authenticateUser(authorizationCode, provider)
            val headers = authResult.headers
            return ResponseEntity.ok()
                .headers(headers)
                .body(
                    CommonResponse.success(
                        data = UserAuthResponse(authResult.userName, authResult.userEmail, authResult.userRole)
                    )
                )
        }

    @PostMapping("/name")
    fun updateUserName() {
        TODO("Not yet implemented")
    }

    // user 정보 조회
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