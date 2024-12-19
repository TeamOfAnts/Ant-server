package com.example.antserver.presentation.auth

import com.example.antserver.application.auth.TokenService
import com.example.antserver.presentation.auth.dto.RefreshRequest
import com.example.antserver.presentation.auth.dto.RefreshResponse
import com.example.antserver.util.response.CommonResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("auth")
class AuthController(
    private val tokenService: TokenService,
) {

    @GetMapping("/refresh")
    fun refreshAccessToken(
        request: HttpServletRequest,
        @RequestBody refreshRequest: RefreshRequest
    ): CommonResponse<RefreshResponse> {
        val currentAccessToken = tokenService.getAccessToken(request)
        val userId = UUID.fromString(tokenService.parseClaimsWithoutVerify(currentAccessToken))
        val newAccessToken = tokenService.refreshAccessToken(userId, refreshRequest.refreshToken)
        return CommonResponse(RefreshResponse.of(newAccessToken))
    }
}