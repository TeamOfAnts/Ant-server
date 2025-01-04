package com.example.antserver.presentation.user.dto.oauth.google

import com.example.antserver.presentation.user.dto.oauth.ProfileResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleProfileResponse(
    val sub: String,
    val emailVerified: Boolean,
    override val name: String,
    override val email: String,
    override val providerId: String = sub,
): ProfileResponse