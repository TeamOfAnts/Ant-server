package com.example.antserver.domain.user

import com.example.antserver.domain.AggregateRoot
import com.fasterxml.uuid.Generators
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "`user`", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
    @Id
    @Column(name = "id")
    val id: UUID = Generators.timeBasedEpochGenerator().generate(), // v7

    @Column(name = "name")
    var name: String,

    @Column(name = "email", unique = true)
    val email: String,

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    val provider: ProviderType,

    @Column(name = "providerId")
    val providerId: String,

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var role: UserRoleType,

    @Column(name = "deletedAt")
    val deletedAt: Instant? = null
): AggregateRoot() {

    fun updateName(newName: String) {
        this.name = newName
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