package com.example.antserver.util.oauth

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.presentation.user.dto.oauth.ProfileResponse

interface OAuthClient {
    val providerType: ProviderType
    fun getAccessToken(authorizationCode: String): String
    fun getUserProfile(accessToken: String): ProfileResponse
}