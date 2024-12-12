package com.example.antserver.util.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val expirationTime: ExpirationTime
) {
    data class ExpirationTime(
        val access: Long,
        val refresh: Long
    )
}