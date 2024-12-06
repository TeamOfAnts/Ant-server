package com.example.antserver.presentation.dto.user

import domain.user.UserRoleType

data class UserAuthResponse(
    val userName: String?,
    val userEmail: String,
    val userRole: UserRoleType
) { companion object
    fun from(userAuthResult: UserAuthResult): UserAuthResponse {
        return UserAuthResponse(
            userName = userAuthResult.userName,
            userEmail = userAuthResult.userEmail,
            userRole = userAuthResult.userRole)
    }
}