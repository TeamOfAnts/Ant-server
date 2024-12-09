package com.example.antserver.domain.user

import com.example.antserver.util.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    val name: String,

    @Column(name = "email")
    val email: String,

    @Column(name = "provider")
    val provider: ProviderType,

//    @Column(name = "providerId")
//    val providerId: String,

    @Column(name = "role")
    var role: UserRoleType,

    @Column(name = "deletedAt")
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
            name: String,
            email: String,
            provider: ProviderType,
            role: UserRoleType
        ): User {
            return User(
                name = name,
                email = email,
                provider = provider,
                role = role,
            )
        }
    }
}