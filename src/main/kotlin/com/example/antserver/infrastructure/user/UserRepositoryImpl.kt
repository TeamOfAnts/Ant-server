package com.example.antserver.infrastructure.user

import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.infrastructure.user.JpaUserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
): UserRepository {
    override fun save(user: User): User {
        return jpaUserRepository.save(user)
    }
    override fun findByEmail(email: String): User {
        TODO("Not yet implemented")
    }

    override fun findByProviderId(providerId: String): User {
        TODO("Not yet implemented")
    }
}