package com.example.antserver.presentation.dto.user

import com.example.antserver.domain.user.ProviderType

data class UserAuthRequest(
    val authorizationCode: String,
    val provider: ProviderType
)