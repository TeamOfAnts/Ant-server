package com.example.antserver.presentation.dto.user

import com.example.antserver.domain.user.UserRoleType

data class UserAuthResult(
    val accessToken: String,
    val refreshToken: String,
    val userName: String,
    val userEmail: String,
    val userRole: UserRoleType
)
