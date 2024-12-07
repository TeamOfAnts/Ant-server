package com.example.antserver.util

import com.example.antserver.util.response.CommonResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<CommonResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CommonResponse.fail(
                errorMessage = e.message ?: "Invalid input"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<CommonResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            CommonResponse.fail(
                errorMessage = "An unexpected error occurred"
            )
        )
    }
}