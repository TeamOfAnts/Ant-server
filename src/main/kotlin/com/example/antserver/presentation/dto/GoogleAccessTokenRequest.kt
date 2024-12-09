package com.example.antserver.presentation.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleAccessTokenRequest(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
//    val responseType: String,
//    val scope: String,
    val code: String,
//    val accessType: String,
    val grantType: String,
)