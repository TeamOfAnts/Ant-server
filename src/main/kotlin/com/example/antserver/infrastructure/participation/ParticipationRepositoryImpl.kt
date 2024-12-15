package com.example.antserver.infrastructure.participation

import com.example.antserver.domain.participation.Participation
import com.example.antserver.domain.participation.ParticipationRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ParticipationRepositoryImpl(
    private val jpaParticipationRepository: JpaParticipationRepository,
): ParticipationRepository {

    override fun save(participation: Participation): Participation {
        return jpaParticipationRepository.save(participation)
    }

    override fun findAllByUserId(userId: UUID): List<Participation> {
        return jpaParticipationRepository.findAllByUserId(userId)
    }
}