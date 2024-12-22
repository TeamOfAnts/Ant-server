package com.example.antserver.presentation.user.dto

data class UserAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNew: Boolean
) {
    companion object {
        fun of(accessToken: String, refreshToken: String, isNew: Boolean): UserAuthResponse {
            return UserAuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                isNew = isNew
            )
        }
    }
}
