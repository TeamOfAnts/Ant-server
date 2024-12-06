package com.example.antserver.domain.user

import domain.user.UserRoleType
import util.BaseEntity
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String? = null,
    val email: String,
    val provider: ProviderType,
//    val providerId: String,
    var role: UserRoleType,
    val deletedAt: Instant? = null
): BaseEntity() {
    fun registUser(user: User) {

    }

    fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    fun deleteUser(user: User) {
        TODO("Not yet implemented")
    }

    fun updateRole(newRole: UserRoleType) {
        this.role = newRole
    }

    companion object {
        fun of(
            email: String,
            provider: ProviderType,
            role: UserRoleType
        ): User {
            return User(
                email = email,
                provider = provider,
                role = role,
            )
        }
    }
}