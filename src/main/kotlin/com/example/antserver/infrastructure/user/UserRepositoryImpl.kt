package com.example.antserver.infrastructure.user

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
): UserRepository {
    override fun save(user: User): User {
        return jpaUserRepository.save(user)
    }

    override fun findById(userId: UUID): User? {
        return jpaUserRepository.findById(userId).orElse(null)
    }

    override fun findByEmailAndProvider(email: String, provider: ProviderType): User? {
        return jpaUserRepository.findByEmailAndProvider(email, provider)
    }
}