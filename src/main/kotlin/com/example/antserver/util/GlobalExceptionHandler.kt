package com.example.antserver.util

import com.example.antserver.util.response.CommonResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): CommonResponse<String> {
        return CommonResponse.fail(
                errorMessage = e.message ?: "Invalid input"
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): CommonResponse<String> {
        return CommonResponse.fail(
                errorMessage = e.message ?: "An unexpected error occurred"
        )
    }
}