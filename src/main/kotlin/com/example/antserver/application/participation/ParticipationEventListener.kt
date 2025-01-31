package com.example.antserver.application.participation

import com.example.antserver.application.schedule.ScheduleConfirmedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ParticipationEventListener(
    private val participationService: ParticipationService
) {
    @EventListener
    fun handleScheduleConfirmedEvent(event: ScheduleConfirmedEvent) {
        participationService.saveParticipation(event)
    }
}