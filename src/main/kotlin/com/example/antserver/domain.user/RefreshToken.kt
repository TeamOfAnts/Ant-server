package com.example.antserver.domain.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import util.BaseEntity
import java.util.Base64
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    private var token: String,
    private var revoke: Boolean
): BaseEntity() {

    fun updateToken(newToken: String): RefreshToken {
        return this.copy(token = newToken, revoke = false)
    }

    fun isRevoked(): Boolean {
        val payloadMapper: ObjectMapper = jacksonObjectMapper()
        try {
            val parts = token.split(".")
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val payloadMap: Map<String, Any> = payloadMapper.readValue(payload)

            // exp 필드 확인
            val exp = (payloadMap["exp"] as? Number)?.toLong()
                ?: throw IllegalArgumentException("Missing or invalid 'exp' claim")

            // 현재 시간과 비교
            val currentTime = System.currentTimeMillis() / 1000 // 초 단위 현재 시간
            val isRevoked = currentTime > exp // 현재 시간이 만료 시간보다 크면 만료된 것

            // 만료 처리
            revoke = isRevoked
            return isRevoked
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to validate token: ${e.message}")
        }
    }
}