package com.example.antserver.application.poll

import com.example.antserver.domain.poll.PollRepository
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
    fun triggerPollGeneration() {
        val lastPollDate = pollRepository.findLast().createdAt
        val today = LocalDate.now()

        if (lastPollDate == null || ChronoUnit.WEEKS.between(lastPollDate, today) >= 2) {
            pollService.generatePoll()
        }
    }
}