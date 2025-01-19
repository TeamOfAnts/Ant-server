package com.example.antserver.application

import com.example.antserver.application.poll.PollService
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.schedule.ScheduleOn
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.infrastructure.schedule.JpaScheduleRepository
import com.example.antserver.infrastructure.user.JpaUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleServiceTest {

    @Autowired
    private lateinit var jpaScheduleRepository: JpaScheduleRepository

    @Autowired
    private lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Test
    @DisplayName("Poll이 생성되면 해당 Poll에 대한 Schedules가 생성된다")
    fun testPollGenerateSchedules() {
        // given
        val poll = pollService.generatePoll()

        // when
        val schedules = jpaScheduleRepository.findAllByPollId(poll.id!!)

        // then
        assertThat(schedules).hasSize(15)
        assertThat(schedules[0].scheduleOn).isEqualTo(ScheduleOn.Scheduled(poll.startAt.plus(4, ChronoUnit.DAYS)))
        assertThat(schedules[13].scheduleOn).isEqualTo(ScheduleOn.Scheduled(poll.startAt.plus(17, ChronoUnit.DAYS)))
        assertThat(schedules[14].scheduleOn).isEqualTo(ScheduleOn.Unscheduled)
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
        assertThat(schedules.size).isEqualTo(15)
        assertThat(schedules.all { it.pollId == poll.id }).isTrue
    }

    @Test
    @DisplayName("특정 scheduleId에 투표한다")
    fun voteSchedules() {
        // given
        pollService.generatePoll()
        val userName = "test"
        val user = User.of(userName, "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        jpaUserRepository.save(user)
        val voteScheduleIds = listOf(1L, 3L, 5L)

        // when
        val updatedSchedules = scheduleService.voteSchedules(user.id, voteScheduleIds)

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
    fun modifyVotes() {
        // given
        val userName = "test"
        val user = User.of(userName, "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        jpaUserRepository.save(user)
        pollService.generatePoll()

        val firstVoteScheduleIds = listOf(1L, 3L, 5L)
        val secondVoteScheduleIds = listOf(1L, 7L, 6L)

        // when
        scheduleService.voteSchedules(user.id, firstVoteScheduleIds)
        val updatedSchedules = scheduleService.voteSchedules(user.id, secondVoteScheduleIds)

        // then
        assertThat(updatedSchedules.filter { it.id in secondVoteScheduleIds }
            .all { it.voters.keys.contains(user.id) && it.voters.size == 1 }).isTrue
        assertThat(updatedSchedules.filter { it.id in secondVoteScheduleIds }
            .all { it.voters.values.contains(userName) && it.voters.size == 1 }).isTrue
        assertThat(updatedSchedules.filter { it.id in firstVoteScheduleIds - secondVoteScheduleIds }
            .all { it.voters.isEmpty() }).isTrue
    }

    @Test
    @DisplayName("투표 생성일로부터 이틀이 지나면 투표를 종료하고 스케쥴을 확정한다")
    fun confirmSchedule() {
        // given
        val poll = pollService.generatePoll()
        val user1 = User.of("test1", "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user2 = User.of("test2", "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user3 = User.of("test3", "test3@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user4 = User.of("test4", "test4@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user5 = User.of("test5", "test5@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        jpaUserRepository.save(user1)
        jpaUserRepository.save(user2)
        jpaUserRepository.save(user3)
        jpaUserRepository.save(user4)
        jpaUserRepository.save(user5)
        scheduleService.voteSchedules(user1.id, listOf(1L, 3L, 5L))
        scheduleService.voteSchedules(user2.id, listOf(1L, 3L, 5L))
        scheduleService.voteSchedules(user3.id, listOf(1L, 3L, 6L))
        scheduleService.voteSchedules(user4.id, listOf(1L, 4L, 7L))
        scheduleService.voteSchedules(user5.id, listOf(1L, 4L, 8L))


        // when
        scheduleService.updateScheduleStatus(poll.id!!)

        // then
        val updatedSchedules = jpaScheduleRepository.findAllByPollId(poll.id!!)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.CONFIRMED }).isEqualTo(2)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.DROPPED }).isEqualTo(13)
    }
}
