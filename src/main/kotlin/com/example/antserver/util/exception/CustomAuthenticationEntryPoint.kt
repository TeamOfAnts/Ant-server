package com.example.antserver.util.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint: AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        val errorMessage = request?.getAttribute("customAuthErrorMessage") as String?
            ?: "인증 오류입니다."
        response?.contentType = "application/json"
        response?.characterEncoding = "UTF-8"
        response?.status = HttpServletResponse.SC_UNAUTHORIZED
        response?.writer?.write("""
            {
                "data": {
                    "errorMessage": "$errorMessage"
                }
            }
        """.trimIndent())
        response?.writer?.flush()
    }
}