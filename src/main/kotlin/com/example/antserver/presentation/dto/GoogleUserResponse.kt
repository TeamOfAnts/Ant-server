package com.example.antserver.presentation.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleUserResponse(
    val sub: String,
    val email: String,
    val emailVerified: String,
    val name: String,
    val exp: String
)