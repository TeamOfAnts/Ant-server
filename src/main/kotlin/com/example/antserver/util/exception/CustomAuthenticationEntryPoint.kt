package com.example.antserver.util.exception

import com.example.antserver.util.log.logger
import com.example.antserver.util.response.writeErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val logger = CustomAuthenticationEntryPoint::class.logger()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warn(authException.message)
        val errorMessage = "인증 오류입니다."
        val status = HttpServletResponse.SC_UNAUTHORIZED
        writeErrorResponse(response, status, errorMessage)
    }
}