package com.example.antserver

import com.example.antserver.application.schedule.ScheduleScheduler
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.mockito.Mockito
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
class ScheduleSchedulerIntegrationTest {

    @Autowired
    private lateinit var scheduleScheduler: ScheduleScheduler

    @MockBean
    private lateinit var scheduleService: ScheduleService

    @MockBean
    private lateinit var pollRepository: PollRepository

    @Test
    fun `triggerScheduleUpdate should call updateScheduleStatus when poll is older than 2 weeks`() {
        // given
        val startAt = Instant.now().minus(17, ChronoUnit.DAYS)
        val endAt = startAt.plus(13, ChronoUnit.DAYS)
        val lastPoll = Poll(id = 1L, title = "test", description = "test", startAt = startAt, endAt = endAt, pollStatus = PollStatus.OPEN)

        Mockito.`when`(pollRepository.findLast()).thenReturn(lastPoll)

        // Act
        scheduleScheduler.triggerScheduleUpdate()

        // Assert
        Mockito.verify(scheduleService, Mockito.times(1)).updateScheduleStatus(lastPoll.id!!)
    }
}