package com.example.antserver.fake

import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRepository
import com.fasterxml.uuid.Generators
import java.util.*

class FakeUserRepository: UserRepository {
    private val data: MutableList<User> = mutableListOf()

    override fun save(user: User): User {
        val existingIndex = data.indexOfFirst { it.id == user.id }

        return if (existingIndex != -1) {
            data[existingIndex] = user
            user
        } else {
            val newUser = user.copy(id = Generators.timeBasedEpochGenerator().generate())
            data.add(newUser)
            newUser
        }
    }

    fun saveAll(users: List<User>) {
        data.addAll(users)
    }

    override fun findById(userId: UUID): User? {
        return data.find { it.id == userId }
    }

    override fun findByEmailAndProvider(email: String, provider: ProviderType): User? {
        return data.find { it.email == email && it.provider == provider }
    }

    fun clear() {
        data.clear()
    }
}