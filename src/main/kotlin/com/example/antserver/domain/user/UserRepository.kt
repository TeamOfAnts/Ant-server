package com.example.antserver.domain.user

import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(userId: UUID): User?
    fun findByEmail(email: String): User
//    fun findByProviderId(providerId: String): User
}