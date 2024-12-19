package com.example.antserver.infrastructure.refreshtoken

import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRefreshTokenRepository: JpaRefreshTokenRepository
): RefreshTokenRepository {

    override fun save(refreshToken: RefreshToken): RefreshToken {
        return jpaRefreshTokenRepository.save(refreshToken)
    }

    override fun findByUserId(userId: UUID): RefreshToken? {
        return jpaRefreshTokenRepository.findByUserId(userId)
    }

    override fun findByToken(token: String): RefreshToken? {
        return jpaRefreshTokenRepository.findByToken(token)
    }
}