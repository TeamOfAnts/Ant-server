package com.example.antserver.application.schedule

import org.springframework.context.event.EventListener
import com.example.antserver.application.poll.PollGeneratedEvent
import org.springframework.stereotype.Component

@Component
class ScheduleEventListener(
    private val scheduleService: ScheduleService
) {
    @EventListener
    fun handlePollGeneratedEvent(event: PollGeneratedEvent) {
        scheduleService.generateSchedules(event)
    }
}
