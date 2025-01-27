package com.example.antserver.application

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.application.user.UserService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleOn
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import com.example.antserver.fake.FakePollRepository
import com.example.antserver.fake.FakeScheduleRepository
import com.example.antserver.testconfig.TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@Import(TestConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleServiceTest {

    @Autowired
    private lateinit var pollRepository: FakePollRepository

    @Autowired
    private lateinit var scheduleRepository: FakeScheduleRepository

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @MockBean
    private lateinit var userService: UserService

    private lateinit var user: User
    private lateinit var poll: Poll
    private lateinit var schedules: List<Schedule>
    private val voteStartAt = LocalDate.of(2025, 1, 23).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant() // 목
    private val voteEndAt = voteStartAt.plus(2, ChronoUnit.DAYS) // 토
    private val scheduleStartAt = voteStartAt.plus(4, ChronoUnit.DAYS) // 월
    private val scheduleEndAt = voteStartAt.plus(17, ChronoUnit.DAYS) // 일

    @BeforeEach
    fun set() {
        scheduleRepository.clear()
        pollRepository.clear()

        user = User.of("test", "test@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        given(userService.findUser(user.id)).willReturn(user)

        poll = Poll.of(
            id = 1L,
            voteStartAt = voteStartAt,
            voteEndAt = voteEndAt,
            scheduleStartAt = scheduleStartAt,
            scheduleEndAt = scheduleEndAt
        )
        pollRepository.save(poll)

        val daysWithUnscheduled = (ChronoUnit.DAYS.between(scheduleStartAt, scheduleEndAt) + 1) + 1
        schedules = Schedule.ofSchedules(poll.id!!, scheduleStartAt, daysWithUnscheduled)
            .mapIndexed { index, schedule -> schedule.copy(id = index + 1L) }
        scheduleRepository.saveAll(schedules)
    }

    @Test
    @DisplayName("Poll이 생성되면 해당 Poll에 대한 Schedules가 생성된다")
    fun testPollGenerateSchedules() {
        // given
        val poll = poll
        val scheduleStartDate = scheduleStartAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
        val scheduleStartDay = scheduleStartDate.dayOfWeek

        // when
        val schedules = scheduleRepository.findAllByPollId(poll.id!!)

        // then
        assertThat(schedules).hasSize(19)
        assertThat(schedules[0].scheduleOn).isEqualTo(ScheduleOn.Scheduled(scheduleStartDate, scheduleStartDay))
        assertThat(schedules[5].scheduleOn).isEqualTo(ScheduleOn.Scheduled(scheduleStartDate.plusDays(5), scheduleStartDay.plus(5), "낮"))
        assertThat(schedules[18].scheduleOn).isEqualTo(ScheduleOn.Unscheduled)
        schedules.forEach { schedule ->
            assertThat(schedule.pollId).isEqualTo(poll.id)
            assertThat(schedule.scheduleStatus).isEqualTo(ScheduleStatus.VOTING)
        }
    }

    @Test
    @DisplayName("pollId로 해당 Poll에 대한 모든 Schedule을 조회한다")
    fun testFindSchedulesByPollId() {
        // given
        val poll = poll

        // when
        val schedules = scheduleService.findSchedulesByPollId(poll.id!!)

        // then
        assertThat(schedules.size).isEqualTo(19)
        assertThat(schedules.all { it.pollId == poll.id }).isTrue
    }

    @Test
    @DisplayName("특정 scheduleId에 투표한다")
    fun voteSchedules() {
        // given
        val voteScheduleIds = listOf(1L, 3L, 5L)

        // when
        schedules = scheduleService.voteSchedules(user.id, voteScheduleIds)

        // then
        assertThat(schedules.filter { it.id in listOf(1L, 3L, 5L) }
            .all { it.voters.keys.contains(user.id) && it.voters.size == 1 }).isTrue
        assertThat(schedules.filter { it.id in listOf(1L, 3L, 5L) }
            .all { it.voters.values.contains(user.name) && it.voters.size == 1 }).isTrue
        assertThat(schedules.filterNot { it.id in listOf(1L, 3L, 5L) }
            .none { it.voters.keys.contains(user.id) && it.voters.size == 0 }).isTrue
    }

    @Test
    @DisplayName("특정 scheduleId에 대한 투표를 수정한다")
    fun modifyVotes() {
        // given
        val firstVoteScheduleIds = listOf(1L, 3L, 5L)
        val secondVoteScheduleIds = listOf(1L, 7L, 6L)

        // when
        schedules = scheduleService.voteSchedules(user.id, firstVoteScheduleIds)
        schedules = scheduleService.voteSchedules(user.id, secondVoteScheduleIds)

        // then
        assertThat(schedules.filter { it.id in secondVoteScheduleIds }
            .all { it.voters.keys.contains(user.id) && it.voters.size == 1 }).isTrue
        assertThat(schedules.filter { it.id in secondVoteScheduleIds }
            .all { it.voters.values.contains(user.name) && it.voters.size == 1 }).isTrue
        assertThat(schedules.filter { it.id in firstVoteScheduleIds - secondVoteScheduleIds }
            .all { it.voters.isEmpty() }).isTrue
    }

    @Test
    @DisplayName("투표 생성일로부터 이틀이 지나면 투표를 종료하고 스케쥴을 확정한다")
    fun confirmSchedule() {
        // given
        val poll = poll
        val user1 = User.of("test1", "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user2 = User.of("test2", "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user3 = User.of("test3", "test3@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user4 = User.of("test4", "test4@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user5 = User.of("test5", "test5@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        given(userService.findUser(user1.id)).willReturn(user1)
        given(userService.findUser(user2.id)).willReturn(user2)
        given(userService.findUser(user3.id)).willReturn(user3)
        given(userService.findUser(user4.id)).willReturn(user4)
        given(userService.findUser(user5.id)).willReturn(user5)

        scheduleService.voteSchedules(user1.id, listOf(1L, 3L, 5L))
        scheduleService.voteSchedules(user2.id, listOf(1L, 3L, 5L))
        scheduleService.voteSchedules(user3.id, listOf(1L, 3L, 6L))
        scheduleService.voteSchedules(user4.id, listOf(1L, 4L, 7L))
        scheduleService.voteSchedules(user5.id, listOf(1L, 4L, 8L))

        // when
        scheduleService.updateScheduleStatus(poll.id!!)

        // then
        val updatedSchedules = scheduleRepository.findAllByPollId(poll.id!!)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.CONFIRMED }).isEqualTo(2)
        assertThat(updatedSchedules.count { it.scheduleStatus == ScheduleStatus.DROPPED }).isEqualTo(17)
    }
}
