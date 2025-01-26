package com.example.antserver.application.schedule

import com.example.antserver.application.poll.PollGeneratedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ScheduleEventListener(
    private val scheduleService: ScheduleService
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePollGeneratedEvent(event: PollGeneratedEvent) {
        scheduleService.generateSchedules(event)
    }
}
