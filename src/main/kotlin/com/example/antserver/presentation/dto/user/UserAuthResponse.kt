package com.example.antserver.presentation.dto.user

data class UserAuthResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun of(accessToken: String, refreshToken: String): UserAuthResponse {
            return UserAuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }
}
