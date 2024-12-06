package com.example.antserver.infrastructure.refreshtoken

import com.example.antserver.domain.user.RefreshToken
import com.example.antserver.domain.user.RefreshTokenRepository
import com.example.antserver.infrastructure.refreshtoken.JpaRefreshTokenRepository
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