package com.example.antserver.infrastructure.refreshtoken

import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRefreshTokenRepository: JpaRefreshTokenRepository
): RefreshTokenRepository {
    override fun findByUserId(userId: UUID): RefreshToken {
        TODO("Not yet implemented")
    }
}