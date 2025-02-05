package com.example.antserver.application.participation

import com.example.antserver.application.schedule.ScheduleConfirmedEvent
import com.example.antserver.domain.participation.Participation
import com.example.antserver.domain.participation.ParticipationRepository
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
    fun saveParticipation(event: ScheduleConfirmedEvent): List<Participation> {
        val participations = event.confirmedSchedules.flatMap { schedule ->
            Participation.from(schedule)
        }
        return participationRepository.saveAll(participations)
    }
}