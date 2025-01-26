package com.example.antserver.fake

import com.example.antserver.domain.auth.RefreshToken
import com.example.antserver.domain.auth.RefreshTokenRepository
import com.fasterxml.uuid.Generators
import java.util.*

class FakeRefreshTokenRepository: RefreshTokenRepository {
     val data: MutableList<RefreshToken> = mutableListOf()

    override fun save(refreshToken: RefreshToken): RefreshToken {
        val existingIndex = data.indexOfFirst { it.id == refreshToken.id }

        return if (existingIndex != -1) {
            data[existingIndex] = refreshToken
            refreshToken
        } else {
            val newRefreshToken = refreshToken.copy(id = Generators.timeBasedEpochGenerator().generate())
            data.add(newRefreshToken)
            newRefreshToken
        }
    }

    override fun findByUserId(userId: UUID): RefreshToken? {
        return data.find { it.userId == userId }
    }

    override fun findByToken(token: String): RefreshToken? {
        return data.find { it.token == token }
    }

    fun clear() {
        data.clear()
    }
}