package com.example.antserver.application

import com.example.antserver.application.participation.ParticipationService
import com.example.antserver.application.poll.PollService
import com.example.antserver.application.schedule.ScheduleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import kotlin.test.Test

@SpringBootTest
class ParticipationTest {
    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var participationService: ParticipationService

    @Test
    @DisplayName("스케쥴 확정 시 participation이 저장된다")
    fun findParticipation() {
        // given: 1, 2, 3번 스케쥴은 3표 이상이므로 CONFIRMED, 4, 5, 6, 7번은 DROPPED
        val poll = pollService.generatePoll()
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()
        val userId4 = UUID.randomUUID()
        val userId5 = UUID.randomUUID()
        scheduleService.voteSchedules(userId1, listOf(1L, 2L, 3L))
        scheduleService.voteSchedules(userId2, listOf(1L, 2L, 4L))
        scheduleService.voteSchedules(userId3, listOf(1L, 2L, 5L))
        scheduleService.voteSchedules(userId4, listOf(1L, 3L, 6L))
        scheduleService.voteSchedules(userId5, listOf(3L, 4L, 7L))

        // when
        scheduleService.updateScheduleStatus(poll.id!!)
        val participationsOfUser1 = participationService.findParticipation(userId1)
        val participationsOfUser2 = participationService.findParticipation(userId2)
        val participationsOfUser3 = participationService.findParticipation(userId3)
        val participationsOfUser4 = participationService.findParticipation(userId4)
        val participationsOfUser5 = participationService.findParticipation(userId5)

        // then
        assertThat(participationsOfUser1.size).isEqualTo(3) // 1, 2, 3
        assertThat(participationsOfUser2.size).isEqualTo(2) // 1, 2
        assertThat(participationsOfUser3.size).isEqualTo(2) // 1, 2
        assertThat(participationsOfUser4.size).isEqualTo(2) // 1, 3
        assertThat(participationsOfUser5.size).isEqualTo(1) // 1
    }
}