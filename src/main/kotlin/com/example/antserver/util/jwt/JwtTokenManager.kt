package com.example.antserver.util.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import com.example.antserver.util.config.JwtProperties
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.stereotype.Component

@Component
class JwtTokenManager(
    private val jwtProperties: JwtProperties
) {

    fun parseClaims(accessToken: String): String {
        return JWT.require(Algorithm.HMAC512(jwtProperties.secret))
            .build()
            .verify(accessToken)
            .getClaim(jwtProperties.claim)
            ?.asString()
            ?: throw ApplicationException(Status.Unauthorized, "Unable to parse the userId from the Access Token.", "인증 오류입니다.")
    }

    fun parseClaimsWithoutVerify(accessToken: String): String {
        return JWT.decode(accessToken)
            .getClaim(jwtProperties.claim)
            ?.asString()
            ?: throw ApplicationException(Status.Unauthorized, "Unable to parse the userId from the Access Token.", "인증 오류입니다.")
    }

    fun getAccessToken(request: HttpServletRequest): String {
        return request.getHeader(HttpHeaders.AUTHORIZATION)
            .takeIf { it.startsWith(jwtProperties.bearerPrefix) }
            ?.removePrefix(jwtProperties.bearerPrefix)
            ?.trim()
            ?: throw ApplicationException(Status.Unauthorized, "Unable to get access token from header", "인증 오류입니다.")
    }
}