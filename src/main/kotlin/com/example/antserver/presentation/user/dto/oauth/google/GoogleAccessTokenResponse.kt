package com.example.antserver.presentation.user.dto.oauth.google

import com.example.antserver.presentation.user.dto.oauth.AccessTokenResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleAccessTokenResponse(
    val idToken: String,
    override val accessToken: String = idToken
): AccessTokenResponse