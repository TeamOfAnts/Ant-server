package com.example.antserver.application

import com.example.antserver.application.poll.PollService
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.application.user.UserService
import com.example.antserver.domain.schedule.ScheduleRepository
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.infrastructure.user.JpaUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Test
    @DisplayName("Poll이 생성되면 해당 Poll에 대한 Schedules가 생성된다")
    fun testPollGenerateSchedules() {
        // given
        val poll = pollService.generatePoll()

        // when
        val schedules = scheduleRepository.findAllByPollId(poll.id!!)

        // then
        assertThat(schedules).hasSize(14)
        assertThat(schedules[0].scheduleOn).isEqualTo(poll.startAt.plus(1, ChronoUnit.DAYS))
        assertThat(schedules[13].scheduleOn).isEqualTo(poll.startAt.plus(14, ChronoUnit.DAYS))
        schedules.forEach { schedule ->
            assertThat(schedule.pollId).isEqualTo(poll.id)
            assertThat(schedule.scheduleStatus).isEqualTo(ScheduleStatus.VOTING)
        }
    }

    @Test
    @DisplayName("pollId로 해당 Poll에 대한 모든 Schedule을 조회한다")
    fun testFindSchedulesByPollId() {
        // given
        val poll = pollService.generatePoll()

        // when
        val schedules = scheduleService.findSchedulesByPollId(poll.id!!)

        // then
        assertThat(schedules.size).isEqualTo(14)
        assertThat(schedules.all { it.pollId == poll.id }).isTrue
    }

    @Test
    @DisplayName("특정 scheduleId에 투표한다")
    fun voteSchedules() {
        // given
        val userName = "test"
        val user = User.of(userName, "test@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        jpaUserRepository.save(user)
        pollService.generatePoll()

        // when
        val updatedSchedules = scheduleService.voteSchedules(user.id, listOf(1L, 3L, 5L))

        // then
        assertThat(updatedSchedules.filter { it.id in listOf(1L, 3L, 5L) }
            .all { it.voters.keys.contains(user.id) && it.voters.size == 1 }).isTrue
        assertThat(updatedSchedules.filter { it.id in listOf(1L, 3L, 5L) }
            .all { it.voters.values.contains(userName) && it.voters.size == 1 }).isTrue
        assertThat(updatedSchedules.filterNot { it.id in listOf(1L, 3L, 5L) }
            .none { it.voters.keys.contains(user.id) && it.voters.size == 0 }).isTrue
    }

    @Test
    @DisplayName("특정 scheduleId에 대한 투표를 수정한다")
    fun modifyVoteSchedules() {
        // given
        val userId = UUID.randomUUID()
        pollService.generatePoll()
        val firstVoteScheduleIds = listOf(1L, 3L, 5L)
        val secondVoteScheduleIds = listOf(1L, 7L, 6L)

        // when
        scheduleService.voteSchedules(userId, firstVoteScheduleIds)
        val updatedSchedules = scheduleService.voteSchedules(userId, secondVoteScheduleIds)

        // then
        assertThat(updatedSchedules.all { it.voters.contains(userId) && it.voters.size == 1 }).isTrue
//        assertThat(updatedSchedules.none { it.voters.contains(userId) && it.voters.size == 0 }).isTrue
    }


//
//    @Test
//    @DisplayName("투표 생성일로부터 이틀이 지나면 투표를 종료하고 스케쥴을 확정한다")
//    fun confirmSchedule() {
//        // given
//        val poll = pollService.generatePoll()
//        val userId1 = UUID.randomUUID()
//        val userId2 = UUID.randomUUID()
//        val userId3 = UUID.randomUUID()
//        val userId4 = UUID.randomUUID()
//        val userId5 = UUID.randomUUID()
//        scheduleService.voteSchedules(userId1, listOf(1L, 3L, 5L))
//        scheduleService.voteSchedules(userId2, listOf(1L, 3L, 5L))
//        scheduleService.voteSchedules(userId3, listOf(1L, 3L, 6L))
//        scheduleService.voteSchedules(userId4, listOf(1L, 4L, 7L))
//        scheduleService.voteSchedules(userId5, listOf(1L, 4L, 8L))
//
//        // when
//        scheduleService.updateScheduleStatus(poll.id!!)
//
//        // then
//        val updatedSchedules = scheduleRepository.findAllByPollId(poll.id!!)
//        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.CONFIRMED }).isEqualTo(2)
//        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.DROPPED }).isEqualTo(12)
//    }
}
