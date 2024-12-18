package com.example.antserver.application

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleRepository
import com.example.antserver.domain.schedule.ScheduleStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test

@SpringBootTest
class ScheduleServiceTest {

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Test
    @DisplayName("PollGeneratedEvent가 발생하면 Schedule 생성")
    fun testGenerateSchedules() {
        // given
        val pollGeneratedEvent = PollGeneratedEvent(
            pollId = 1L,
            startAt = LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant(),
            endAt = LocalDateTime.now().plusDays(3).atZone(ZoneId.systemDefault()).toInstant()
        )

        // when
        val schedules = scheduleService.generateSchedules(pollGeneratedEvent)

        // then
        assertThat(schedules).isNotEmpty
        assertThat(schedules.size).isEqualTo(5) // startAt부터 endAt까지 5일치
        assertThat(schedules.first().pollId).isEqualTo(pollGeneratedEvent.pollId)
        assertThat(schedules.all { it.scheduleStatus == ScheduleStatus.VOTING }).isTrue
    }

    @Test
    @DisplayName("스케줄 상태 업데이트")
    fun testUpdateScheduleStatus() {
        // given
        val schedules = listOf(
            Schedule.of(1L, Instant.now(), ScheduleStatus.VOTING).apply { addVoter(UUID.randomUUID()) },
            Schedule.of(1L, Instant.now().plus(1, ChronoUnit.DAYS), ScheduleStatus.VOTING).apply { repeat(3) { addVoter(UUID.randomUUID()) } }
        )
        scheduleRepository.saveAll(schedules)

        // when
        scheduleService.updateScheduleStatus(1L)

        // then
        val updatedSchedules = scheduleRepository.findAllByPollId(1L)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.CONFIRMED }).isEqualTo(1)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.DROPPED }).isEqualTo(1)
    }

    @Test
    @DisplayName("pollId로 스케줄 조회")
    fun testFindSchedulesByPollId() {
        // given
        val schedules = listOf(
            Schedule.of(1L, Instant.now(), ScheduleStatus.VOTING),
            Schedule.of(1L, Instant.now().plus(1, ChronoUnit.DAYS), ScheduleStatus.VOTING)
        )
        scheduleRepository.saveAll(schedules)

        // when
        val result = scheduleService.findSchedulesByPollId(1L)

        // then
        assertThat(result).isNotEmpty
        assertThat(result.size).isEqualTo(2)
        assertThat(result.all { it.pollId == 1L }).isTrue
    }

    @Test
    @DisplayName("스케줄에 투표")
    fun testVoteSchedules() {
        // given
        val userId = UUID.randomUUID()
        val schedules = listOf(
            Schedule.of(1L, Instant.now(), ScheduleStatus.VOTING),
            Schedule.of(1L, Instant.now().plus(1, ChronoUnit.DAYS), ScheduleStatus.VOTING)
        )
        val savedSchedules = scheduleRepository.saveAll(schedules)

        // when
        val updatedSchedules = scheduleService.voteSchedules(userId, savedSchedules.map { it.id!! })

        // then
        assertThat(updatedSchedules).isNotEmpty
        assertThat(updatedSchedules.all { it.voters.contains(userId) }).isTrue
        assertThat(updatedSchedules.size).isEqualTo(savedSchedules.size)
    }
}
