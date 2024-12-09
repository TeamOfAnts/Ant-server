package com.example.antserver.presentation.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleAccessTokenResponse(
    val accessToken: String,
    val expiresIn: String,
    val refreshToken: String,
    val scope: String,
    val tokenType: String,
    val idToken: String
)