package com.example.antserver.infrastructure.user

import com.example.antserver.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface JpaUserRepository: JpaRepository<User, UUID> {
    fun save(user: User): User
    fun findByEmail(email: String): User?
}