package com.example.antserver.infrastructure.refreshtoken

import com.example.antserver.domain.auth.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaRefreshTokenRepository: JpaRepository<RefreshToken, UUID> {
    fun findByUserId(userId: UUID)
}