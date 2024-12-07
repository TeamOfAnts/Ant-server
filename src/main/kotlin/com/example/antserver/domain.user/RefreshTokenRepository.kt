package com.example.antserver.domain.user

import java.util.*

interface RefreshTokenRepository {
    fun findByUserId(userId: UUID): RefreshToken
}