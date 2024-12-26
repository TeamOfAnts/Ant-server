package com.example.antserver.application.participation

import com.example.antserver.application.schedule.ScheduleStatusChangedEvent
import com.example.antserver.domain.participation.Participation
import com.example.antserver.domain.participation.ParticipationRepository
import com.example.antserver.domain.participation.ParticipationType
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
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
    fun saveParticipation(event: ScheduleStatusChangedEvent): List<Participation> {
        val participations = event.confirmedScheduleVoters.flatMap { schedule ->
            schedule.voters.map { userId ->
                Participation.of(
                    scheduleId = schedule.id ?: throw ApplicationException(Status.ServerError, "Schedule id is null", ""),
                    userId = userId,
                    participationType = ParticipationType.STUDY
                )
            }
        }

        return participationRepository.saveAll(participations)
    }
}