package com.example.antserver.presentation.auth

import com.example.antserver.application.auth.TokenService
import com.example.antserver.presentation.auth.dto.RefreshRequest
import com.example.antserver.presentation.auth.dto.RefreshResponse
import com.example.antserver.util.response.CommonResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("auth")
class AuthController(
    private val tokenService: TokenService,
) {

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody refreshRequest: RefreshRequest
    ): CommonResponse<RefreshResponse> {
        val userId = tokenService.findByToken(refreshRequest.refreshToken).userId
        val newAccessToken = tokenService.refreshAccessToken(userId, refreshRequest.refreshToken)
        return CommonResponse(RefreshResponse.of(newAccessToken))
    }
}