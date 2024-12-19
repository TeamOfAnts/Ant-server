package com.example.antserver.util.exception

import com.example.antserver.util.response.CommonResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.security.core.AuthenticationException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): CommonResponse<String> {
        return CommonResponse(
            data = e.message ?: "예상하지 못 한 오류가 발생했습니다."
        )
    }
}