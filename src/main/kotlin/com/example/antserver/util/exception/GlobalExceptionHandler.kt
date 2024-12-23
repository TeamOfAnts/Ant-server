package com.example.antserver.util.exception

import com.example.antserver.util.log.logger
import com.example.antserver.util.response.CommonResponse
import com.example.antserver.util.response.ExceptionResponse
import com.example.antserver.util.response.Status
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = GlobalExceptionHandler::class.logger()

    @ExceptionHandler(Exception::class)
    fun handleGenericException(exception: Exception): ResponseEntity<CommonResponse<ExceptionResponse>> {
        logger.error(exception.message)

        val status = when (exception) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            is BadCredentialsException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(status).body(
            CommonResponse(
                data = ExceptionResponse(
                    errorMessage = exception.message ?: "예상하지 못 한 오류가 발생했습니다."
                )
            )
        )
    }

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(exception: ApplicationException): ResponseEntity<CommonResponse<ExceptionResponse>> {
        logger.error(exception.serverMessage)

        val status = when (exception.status) {
            is Status.BadRequest -> HttpStatus.BAD_REQUEST
            is Status.Unauthorized -> HttpStatus.UNAUTHORIZED
            is Status.NotFound -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(status).body(
            CommonResponse(
                data = ExceptionResponse(
                    errorMessage = exception.message ?: "예상하지 못 한 오류가 발생했습니다."
                )
            )
        )
    }
}