package com.example.antserver.util.oauth

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.stereotype.Component

@Component
class OAuthClientFactory(
    private val clients: List<OAuthClient>
) {

    fun getClient(providerType: ProviderType): OAuthClient {
        return clients.find { it.providerType == providerType }
            ?: throw ApplicationException(Status.BadRequest, "No client found for provider type: $providerType", "제공하지 않는 소셜로그인 타입입니다.")
    }
}