package com.example.antserver.util.response

data class CommonResponse<T>(
    val data: T,
) {
    companion object {
        fun <T> success(data: T): CommonResponse<T> {
            return CommonResponse(
                data = data
            )
        }

        fun <String> fail(errorMessage: String): CommonResponse<String> {
            return CommonResponse(
                data = errorMessage
            )
        }
    }
}