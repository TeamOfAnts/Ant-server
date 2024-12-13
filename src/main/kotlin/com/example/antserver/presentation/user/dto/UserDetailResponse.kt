package com.example.antserver.presentation.user.dto

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType

data class UserDetailResponse(
    private val name: String,
    private val email: String,
    private val provider: ProviderType,
    private val providerId: String,
    private val role: UserRoleType,
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