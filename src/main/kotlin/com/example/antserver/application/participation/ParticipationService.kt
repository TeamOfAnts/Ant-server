package com.example.antserver.application.participation

import com.example.antserver.domain.participation.Participation
import com.example.antserver.domain.participation.ParticipationRepository
import com.example.antserver.domain.participation.ParticipationType
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ParticipationService(
    private val participationRepository: ParticipationRepository,
) {
    fun findParticipation(userId: UUID): List<Participation> {
        return participationRepository.findAllByUserId(userId)
    }

    @Transactional
    @EventListener
    fun saveParticipation(userId: UUID, scheduleId: Long, participationType: ParticipationType): Participation {
        val participation = Participation.of(
            userId,
            scheduleId,
            participationType
        )
        return participationRepository.save(participation)
    }
}