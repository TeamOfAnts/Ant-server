package com.example.antserver.domain.user

import com.example.antserver.util.BaseEntity
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "`users`", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    val name: String,

    @Column(name = "email", unique = true)
    val email: String,

    @Column(name = "provider")
    val provider: ProviderType,

    @Column(name = "providerId")
    val providerId: String,

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
            providerId: String,
            role: UserRoleType
        ): User {
            return User(
                name = name,
                email = email,
                provider = provider,
                providerId = providerId,
                role = role,
            )
        }
    }
}