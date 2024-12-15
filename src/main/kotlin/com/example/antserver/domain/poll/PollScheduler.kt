package com.example.antserver.domain.poll

import com.example.antserver.application.poll.PollService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class PollScheduler(
    private val pollService: PollService,
    private val pollRepository: PollRepository
) {

    @Scheduled(cron = "0 0 18 ? * SUN", zone = "Asia/Seoul")
    fun triggerPoll() {
        val lastPollDate = pollRepository.findLast()
        val today = LocalDate.now()

        if (ChronoUnit.WEEKS.between(lastPollDate, today) >= 2) {
            pollService.generatePoll()
        }
    }
}