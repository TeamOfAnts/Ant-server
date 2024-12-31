package com.example.antserver.util.oauth

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.presentation.user.dto.oauth.google.GoogleAccessTokenResponse
import com.example.antserver.presentation.user.dto.oauth.google.GoogleProfileResponse
import com.example.antserver.util.config.GoogleOAuthProperties
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class Google(
    private val googleOAuthProperties: GoogleOAuthProperties,
    ): OAuthClient {

    override val providerType = ProviderType.GOOGLE

    override fun getAccessToken(authorizationCode: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val googleTokenRequestParams = LinkedMultiValueMap<String, String>()
        googleTokenRequestParams.add("code", authorizationCode)
        googleTokenRequestParams.add("client_id", googleOAuthProperties.clientId)
        googleTokenRequestParams.add("client_secret", googleOAuthProperties.clientSecret)
        googleTokenRequestParams.add("redirect_uri", googleOAuthProperties.redirectUri)
        googleTokenRequestParams.add("grant_type", "authorization_code")
        val googleTokenRequestBody = UriComponentsBuilder.newInstance()
            .queryParams(googleTokenRequestParams)
            .build()
            .query
            .orEmpty()

        val googleTokenRequest = HttpEntity<String>(googleTokenRequestBody, headers)

        return RestTemplate().postForEntity(
            googleOAuthProperties.tokenUrl,
            googleTokenRequest,
            GoogleAccessTokenResponse::class.java
        ).body?.idToken
            ?: throw ApplicationException(Status.Unauthorized, "Invalid Authorization Code ($authorizationCode)", "인증 오류입니다.")
    }

    override fun getUserProfile(accessToken: String): GoogleProfileResponse {
        return RestTemplate().getForEntity(
            googleOAuthProperties.userInfoUrl.replace("{idToken}", accessToken),
            GoogleProfileResponse::class.java
        ).body?.takeIf { it.emailVerified }
            ?: throw ApplicationException(Status.Unauthorized, "Can't get google profile from ${googleOAuthProperties.userInfoUrl} with $accessToken", "인증 오류입니다.")
    }
}