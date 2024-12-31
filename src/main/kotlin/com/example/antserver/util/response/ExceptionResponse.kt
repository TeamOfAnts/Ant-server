package com.example.antserver.util.response

import jakarta.servlet.http.HttpServletResponse

data class ExceptionResponse(
    val errorMessage: String
)

fun writeErrorResponse(response: HttpServletResponse, status: Int, errorMessage: String) {
    response.contentType = "application/json"
    response.characterEncoding = "UTF-8"
    response.status = status
    response.writer.write("""
        {
            "data": {
                "errorMessage": "$errorMessage"
            }
        }
    """.trimIndent())
    response.writer.flush()
}