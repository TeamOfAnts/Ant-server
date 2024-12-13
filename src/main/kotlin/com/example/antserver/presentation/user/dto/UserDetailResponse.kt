package com.example.antserver.presentation.user.dto

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType

data class UserDetailResponse(
    val name: String,
    val email: String,
    val provider: ProviderType,
    val providerId: String,
    val role: UserRoleType,
) {
    companion object {
        fun of(user: User): UserDetailResponse {
            return UserDetailResponse(
                user.name,
                user.email,
                user.provider,
                user.providerId,
                user.role
            )
        }
    }
}