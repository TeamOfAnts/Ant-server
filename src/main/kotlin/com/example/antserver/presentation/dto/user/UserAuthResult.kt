package com.example.antserver.presentation.dto.user

import domain.user.UserRoleType
import org.springframework.http.HttpHeaders

data class UserAuthResult(
    val headers: HttpHeaders,
    val userName: String,
    val userEmail: String,
    val userRole: UserRoleType
)
