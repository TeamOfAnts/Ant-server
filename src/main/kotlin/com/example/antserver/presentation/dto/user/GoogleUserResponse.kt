package com.example.antserver.presentation.dto.user

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleUserResponse(
    val sub: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String,
)