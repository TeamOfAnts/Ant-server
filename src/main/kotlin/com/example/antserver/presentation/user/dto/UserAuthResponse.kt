package com.example.antserver.presentation.user.dto

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
