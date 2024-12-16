package com.example.antserver.application.schedule

import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.util.exception.EmptyResultException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class ScheduleScheduler(
    private val scheduleService: ScheduleService,
    private val pollRepository: PollRepository,
) {

    @Scheduled(cron = "0 0 18 ? * MON", zone = "Asia/Seoul")
    fun triggerScheduleUpdate() {
        val lastPoll = pollRepository.findLast()
        val lastPollDate = lastPoll.createdAt
        val lastPollId = lastPoll.id
            ?: throw EmptyResultException("")
        val today = LocalDate.now()

        if (ChronoUnit.WEEKS.between(lastPollDate, today) >= 2) {
            scheduleService.updateScheduleStatus(lastPollId)
        }
    }
}