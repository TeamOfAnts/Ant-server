package com.example.antserver.domain.auth

import java.util.*

interface RefreshTokenRepository {
    fun findByUserId(userId: UUID): RefreshToken
}