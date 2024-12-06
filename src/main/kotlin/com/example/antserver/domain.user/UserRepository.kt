package com.example.antserver.domain.user

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User
    fun findByProviderId(providerId: String): User
}