package com.example.antserver.application.participation

import com.example.antserver.application.schedule.ScheduleConfirmedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ParticipationEventListener(
    private val participationService: ParticipationService
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleScheduleConfirmedEvent(event: ScheduleConfirmedEvent) {
        participationService.saveParticipation(event)
    }
}