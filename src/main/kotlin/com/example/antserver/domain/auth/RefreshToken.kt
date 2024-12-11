package com.example.antserver.domain.auth

import com.example.antserver.domain.user.User
import com.example.antserver.util.AggregateRoot
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "refresh_token")
data class RefreshToken(
    @Id
    @Column(name = "id")
    val id: UUID = UuidCreator.getTimeOrderedEpoch(), // v7

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