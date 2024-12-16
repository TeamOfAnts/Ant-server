package com.example.antserver.domain.auth

import java.util.*

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByUserId(userId: UUID): RefreshToken?
}