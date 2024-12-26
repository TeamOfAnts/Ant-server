package com.example.antserver

import com.example.antserver.application.schedule.ScheduleScheduler
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ScheduleSchedulerTest {

    private val scheduleService: ScheduleService = mockk(relaxed = true)
    private val pollRepository: PollRepository = mockk()
    private val scheduleScheduler = ScheduleScheduler(scheduleService, pollRepository)

    @Test
    fun `triggerScheduleUpdate should execute correctly`() {
        val lastPollDate = Instant.now().minus(3, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()
        val today = Instant.now()
        val lastPoll = Poll(
            id = 1L,
            "",
            "",
            lastPollDate,
            today,
            PollStatus.OPEN)

        every { pollRepository.findLast() } returns lastPoll

        scheduleScheduler.triggerScheduleUpdate()

        if (ChronoUnit.DAYS.between(lastPollDate, today) == 3L) {
            lastPoll.id?.let { scheduleService.updateScheduleStatus(it) }
        }

        verify(exactly = 1) { scheduleService.updateScheduleStatus(eq(1L)) }
    }
}