package com.example.antserver.domain.user

import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(userId: UUID): User?
    fun findByEmailAndProvider(email: String, provider: ProviderType): User?
}