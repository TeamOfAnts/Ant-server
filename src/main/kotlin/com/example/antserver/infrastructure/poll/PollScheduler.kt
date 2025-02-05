package com.example.antserver.infrastructure.poll

import com.example.antserver.application.poll.PollService
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PollScheduler(
    private val pollService: PollService,
    private val pollRepository: PollRepository,
    private val scheduleService: ScheduleService,
    private val clock: Clock
    ) {

    @Scheduled(cron = "0 0 18 ? * Thu", zone = "Asia/Seoul")
    fun startPoll() {
        val lastPoll = pollRepository.findLast()
        val lastPollDate = lastPoll?.startAt
        val today = Instant.now(clock)

        if (lastPollDate == null || ChronoUnit.DAYS.between(lastPollDate, today) == 14L) {
            pollService.generatePoll()
        }
    }

    @Scheduled(cron = "0 0 18 ? * Sat", zone = "Asia/Seoul")
    fun endPoll() {
        val lastPoll = pollRepository.findLast()
        val lastPollDate = lastPoll?.startAt
        val lastPollId = lastPoll?.id!!
        val today = Instant.now(clock)

        if (ChronoUnit.DAYS.between(lastPollDate, today) == 2L) {
            pollService.updatePollStatus(lastPollId, PollStatus.CLOSED)
            scheduleService.updateScheduleStatus(lastPollId)
        }
    }
}