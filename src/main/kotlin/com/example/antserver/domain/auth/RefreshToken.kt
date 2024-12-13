package com.example.antserver.domain.auth

import com.example.antserver.domain.AggregateRoot
import com.fasterxml.uuid.Generators
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "refresh_token")
data class RefreshToken(
    @Id
    @Column(name = "id")
    val id: UUID = Generators.timeBasedEpochGenerator().generate(), // v7

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "token")
    private var token: String,
): AggregateRoot() {

    companion object {
        fun of(userId: UUID, token: String): RefreshToken {
            return RefreshToken(
                userId = userId,
                token = token
            )
        }
    }

    fun update(token: String) {
        this.token = token
    }
}