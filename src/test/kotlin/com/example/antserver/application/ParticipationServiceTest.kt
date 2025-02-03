package com.example.antserver.application

import com.example.antserver.application.participation.ParticipationService
import com.example.antserver.application.poll.PollService
import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.fake.FakePollRepository
import com.example.antserver.fake.FakeScheduleRepository
import com.example.antserver.fake.FakeUserRepository
import com.example.antserver.testconfig.TestRepositoryConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@Import(TestRepositoryConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ParticipationServiceTest {

    @Autowired
    private lateinit var userRepository: FakeUserRepository

    @Autowired
    private lateinit var pollRepository: FakePollRepository

    @Autowired
    private lateinit var scheduleRepository: FakeScheduleRepository

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var participationService: ParticipationService

    @Test
    @DisplayName("스케쥴 확정 시 participation이 저장된다")
    fun findParticipation() {
        // given: 1, 2, 3번 스케쥴은 3표 이상이므로 CONFIRMED, 4, 5, 6, 7번은 DROPPED
        val user1 = User.of("test1", "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user2 = User.of("test2", "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user3 = User.of("test3", "test3@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user4 = User.of("test4", "test4@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user5 = User.of("test5", "test5@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        userRepository.saveAll(listOf(user1, user2, user3, user4, user5))

        val voteStartAt = Instant.now()
        val voteEndAt = voteStartAt.plus(2, ChronoUnit.DAYS)
        val scheduleStartAt = voteStartAt.plus(4, ChronoUnit.DAYS)
        val scheduleEndAt = voteStartAt.plus(17, ChronoUnit.DAYS)

        val poll = Poll.of(
            id = 1L,
            voteStartAt = voteStartAt,
            voteEndAt = voteEndAt,
            scheduleStartAt = scheduleStartAt,
            scheduleEndAt = scheduleEndAt
        )
        pollRepository.save(poll)

        val daysWithUnscheduled = (ChronoUnit.DAYS.between(scheduleStartAt, scheduleEndAt) + 1) + 1
        val schedules = Schedule.ofSchedules(1L, scheduleStartAt, daysWithUnscheduled)
        scheduleRepository.saveAll(schedules)

        scheduleService.voteSchedules(user1.id, listOf(1L, 2L, 3L))
        scheduleService.voteSchedules(user2.id, listOf(1L, 2L, 4L))
        scheduleService.voteSchedules(user3.id, listOf(1L, 2L, 5L))
        scheduleService.voteSchedules(user4.id, listOf(1L, 3L, 6L))
        scheduleService.voteSchedules(user5.id, listOf(3L, 4L, 7L))

        // when
        scheduleService.updateScheduleStatus(poll.id!!)
        val participationsOfUser1 = participationService.findParticipation(user1.id)
        val participationsOfUser2 = participationService.findParticipation(user2.id)
        val participationsOfUser3 = participationService.findParticipation(user3.id)
        val participationsOfUser4 = participationService.findParticipation(user4.id)
        val participationsOfUser5 = participationService.findParticipation(user5.id)

        // then
        assertThat(participationsOfUser1.size).isEqualTo(3) // 1, 2, 3
        assertThat(participationsOfUser2.size).isEqualTo(2) // 1, 2
        assertThat(participationsOfUser3.size).isEqualTo(2) // 1, 2
        assertThat(participationsOfUser4.size).isEqualTo(2) // 1, 3
        assertThat(participationsOfUser5.size).isEqualTo(1) // 1
    }
}