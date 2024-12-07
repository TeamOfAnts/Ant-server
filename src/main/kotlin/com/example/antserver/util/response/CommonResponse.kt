package com.example.antserver.util.response

data class CommonResponse<T>(
    val data: T? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun <T> success(data: T): CommonResponse<T> {
            return CommonResponse(
                data = data
            )
        }

        fun <T> fail(errorMessage: String?): CommonResponse<T> {
            return CommonResponse(
                errorMessage = errorMessage
            )
        }
    }
}