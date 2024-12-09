package com.example.antserver.presentation.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleUserResponse(
    val iss: String,
    val azp: String,
    val aud: String,
    val sub: String,
    val email: String,
    val emailVerified: String,
    val atHash: String,
    val name: String,
    val picture: String,
    val givenName: String,
    val familyName: String,
    val locale: String,
    val iat: String,
    val exp: String,
    val alg: String,
    val kid: String,
    val typ: String
)