package com.example.antserver.presentation.user.dto.oauth

interface ProfileResponse {
    val name: String
    val email: String
    val providerId: String
}