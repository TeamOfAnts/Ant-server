package com.example.antserver.application.poll

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Component
class PollScheduler(
    private val pollService: PollService,
    private val pollRepository: PollRepository,
    private val scheduleService: ScheduleService,
    ) {
    private val lastPoll = pollRepository.findLast()
    private val lastPollDate = lastPoll?.createdAt

    @Scheduled(cron = "0 0 18 ? * SUN", zone = "Asia/Seoul")
    fun startPoll() {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()

        if (lastPollDate == null || ChronoUnit.WEEKS.between(lastPollDate, today) >= 2) {
            pollService.generatePoll()
        }
    }

    @Scheduled(cron = "0 0 18 ? * TUE", zone = "Asia/Seoul")
    fun endPoll() {
        val lastPollId = lastPoll?.id
            ?: throw ApplicationException(Status.ServerError, "Poll does not exists", "")
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()

        if (ChronoUnit.DAYS.between(lastPollDate, today) == 1L) {
            pollService.updatePollStatus(lastPollId, PollStatus.CLOSED)
            scheduleService.updateScheduleStatus(lastPollId)
        }
    }
}