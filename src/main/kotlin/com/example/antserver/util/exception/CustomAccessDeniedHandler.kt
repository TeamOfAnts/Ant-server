package com.example.antserver.util.exception

import com.example.antserver.util.log.logger
import com.example.antserver.util.response.writeErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler: AccessDeniedHandler {
    private val logger = CustomAuthenticationEntryPoint::class.logger()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        logger.warn(accessDeniedException.message)
        val errorMessage = "인증 오류입니다."
        val status = HttpServletResponse.SC_FORBIDDEN
        writeErrorResponse(response, status, errorMessage)
    }
}