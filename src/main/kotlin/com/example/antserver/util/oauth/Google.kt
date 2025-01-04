package com.example.antserver.util.oauth

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.presentation.user.dto.oauth.google.GoogleAccessTokenResponse
import com.example.antserver.presentation.user.dto.oauth.google.GoogleProfileResponse
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.function.RequestPredicates.contentType
import org.springframework.web.util.UriComponentsBuilder
import java.net.http.HttpHeaders

@Component
class Google(
    private val googleOAuthProperties: GoogleOAuthProperties,
    ): OAuthClient {

    override val providerType = ProviderType.GOOGLE
    private val webClient = WebClient.builder().build()

    override fun getAccessToken(authorizationCode: String): String {
//        val googleAccessTokenResponse = webClient.mutate()
//            .baseUrl(googleOAuthProperties.tokenUrl)
//            .build()
//            .post()
////            .headers { contentType(MediaType.APPLICATION_FORM_URLENCODED) }
//            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//            .accept(MediaType.APPLICATION_JSON)
//            .body(
//                BodyInserters.fromFormData("code", authorizationCode)
//                    .with("client_id", googleOAuthProperties.clientId)
//                    .with("client_secret", googleOAuthProperties.clientSecret)
//                    .with("redirect_uri", googleOAuthProperties.redirectUri)
//                    .with("grant_type", "authorization_code")
//            )
//            .retrieve()
//            .onStatus({ status -> status.isError }) { response ->
//                response.bodyToMono(String::class.java).map { errorBody ->
//                    ApplicationException(Status.BadRequest, "Google API returned error: $errorBody", "인증 오류입니다")
//                }
//            }
//            .bodyToMono(GoogleAccessTokenResponse::class.java)
//            .block()
//
//        return googleAccessTokenResponse?.idToken
//            ?: throw ApplicationException(Status.Unauthorized, "Invalid Authorization Code ($authorizationCode)", "인증 오류입니다")
        val headers = org.springframework.http.HttpHeaders()
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
        val googleProfileResponse = webClient.mutate()
            .build()
            .get()
            .uri(googleOAuthProperties.userInfoUrl.replace("{idToken}", accessToken))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                response.bodyToMono(String::class.java).map { errorBody ->
                    ApplicationException(Status.BadRequest, "Google API returned error: $errorBody", "인증 오류입니다.")
                }
            }
            .bodyToMono(GoogleProfileResponse::class.java)
            .block()

        return googleProfileResponse?.takeIf { it.emailVerified }
            ?: throw ApplicationException(Status.Unauthorized, "Can't get google profile from ${googleOAuthProperties.userInfoUrl} with $accessToken", "인증 오류입니다.")
    }
}