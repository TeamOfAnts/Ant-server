package com.example.antserver.presentation.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.google.gson.annotations.SerializedName

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleAccessTokenRequest(
    @SerializedName("code")
    val code: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    @SerializedName("grant_type")
    val grantType: String,
)