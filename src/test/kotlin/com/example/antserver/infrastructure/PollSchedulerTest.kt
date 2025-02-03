package com.example.antserver.infrastructure

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.fake.FakePollRepository
import com.example.antserver.infrastructure.poll.PollScheduler
import com.example.antserver.testconfig.TestClockConfig
import com.example.antserver.testconfig.TestRepositoryConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.util.ReflectionTestUtils
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@SpringBootTest
@Import(TestClockConfig::class,TestRepositoryConfig::class)
class PollSchedulerTest {
    
    @Autowired
    @Qualifier("testClock")
    private lateinit var clock: Clock

    @Autowired
    private lateinit var pollScheduler: PollScheduler

    @Autowired
    private lateinit var pollRepository: FakePollRepository

    @MockBean
    private lateinit var scheduleService: ScheduleService

    private val firstVoteStartAt = LocalDate.of(2025, 2, 6).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant()
    private val firstVoteEndAt = firstVoteStartAt.plus(2, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        pollRepository.clear()
    }

    @Nested
    @DisplayName("투표 시작")
    inner class PollStartTest {

        @Test
        @DisplayName("첫 투표일에 첫 번째 투표가 생성된다")
        fun createFirstPollOnFirstVoteDate() {
            // given
            clock = Clock.fixed(firstVoteStartAt, ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.startPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("첫 투표일 1주 후, 스케쥴러는 실행되지만 새로운 투표는 생성되지 않는다")
        fun schedulerRunButNoPollCreatedAfterOneWeek() {
            // given
            val previousPoll = Poll.of(
                id = 1L,
                voteStartAt = firstVoteStartAt,
                voteEndAt = firstVoteEndAt,
                scheduleStartAt = firstVoteStartAt.plus(4, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(17, ChronoUnit.DAYS)
            )
            pollRepository.save(previousPoll)

            val noneVotingDay = firstVoteStartAt.plus(7, ChronoUnit.DAYS)
            clock = Clock.fixed(noneVotingDay, ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.startPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(1L)
        }

        @Test
        @DisplayName("첫 투표일 2주 후, 두 번째 투표가 생성된다")
        fun createSecondPollAfterTwoWeeks() {
            // given
            val previousPoll = Poll.of(
                id = 1L,
                voteStartAt = firstVoteStartAt,
                voteEndAt = firstVoteEndAt,
                scheduleStartAt = firstVoteStartAt.plus(4, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(17, ChronoUnit.DAYS)
            )
            pollRepository.save(previousPoll)

            val secondVoteStartAt = firstVoteStartAt.plus(14, ChronoUnit.DAYS)
            clock = Clock.fixed(secondVoteStartAt, ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.startPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(2L)
        }
    }


    @Nested
    @DisplayName("투표 종료")
    inner class EndPollTest {

        @Test
        @DisplayName("첫 번째 투표 시작 이틀 후 투표가 종료된다")
        fun endFirstPollTwoDaysAfterFirstVoteDate() {
            // given
            val poll = Poll.of(
                id = 1L,
                voteStartAt = firstVoteStartAt,
                voteEndAt = firstVoteEndAt,
                scheduleStartAt = firstVoteStartAt.plus(4, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(17, ChronoUnit.DAYS)
            )
            pollRepository.save(poll)

            clock = Clock.fixed(firstVoteEndAt, ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.endPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(poll.id)
            assertThat(pollRepository.findLast()?.pollStatus).isEqualTo(PollStatus.CLOSED)
            verify(scheduleService, times(1)).updateScheduleStatus(poll.id!!)
        }

        @Test
        @DisplayName("첫 번째 투표 종료 1주 후, 스케쥴러는 실행되지만 투표는 종료되지 않는다")
        fun schedulerRunsButNoPollEndedAfterOneWeek() {
            // given
            val previousPoll = Poll.of(
                id = 1L,
                voteStartAt = firstVoteStartAt,
                voteEndAt = firstVoteEndAt,
                scheduleStartAt = firstVoteStartAt.plus(4, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(17, ChronoUnit.DAYS)
            )
            pollRepository.save(previousPoll)

            clock = Clock.fixed(firstVoteEndAt.plus(7, ChronoUnit.DAYS), ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.endPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(previousPoll.id)
            verify(scheduleService, times(0)).updateScheduleStatus(previousPoll.id!!)
        }

        @Test
        @DisplayName("두 번째 투표 시작 이틀 후 투표가 종료된다")
        fun endSecondPollTwoDaysAfterSecondVoteDate() {
            // given
            val previousPoll = Poll.of(
                id = 1L,
                voteStartAt = firstVoteStartAt,
                voteEndAt = firstVoteEndAt,
                scheduleStartAt = firstVoteStartAt.plus(4, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(17, ChronoUnit.DAYS)
            )
            pollRepository.save(previousPoll)

            val currentPoll = Poll.of(
                id = 2L,
                voteStartAt = firstVoteStartAt.plus(14, ChronoUnit.DAYS),
                voteEndAt = firstVoteEndAt.plus(14, ChronoUnit.DAYS),
                scheduleStartAt = firstVoteStartAt.plus(16, ChronoUnit.DAYS),
                scheduleEndAt = firstVoteStartAt.plus(29, ChronoUnit.DAYS)
            )
            pollRepository.save(currentPoll)

            clock = Clock.fixed(firstVoteEndAt.plus(14, ChronoUnit.DAYS), ZoneId.systemDefault())
            ReflectionTestUtils.setField(pollScheduler, "clock", clock)

            // when
            pollScheduler.endPoll()

            // then
            assertThat(pollRepository.findLast()?.id).isEqualTo(currentPoll.id)
            assertThat(pollRepository.findLast()?.pollStatus).isEqualTo(PollStatus.CLOSED)
            verify(scheduleService, times(1)).updateScheduleStatus(currentPoll.id!!)
        }
    }
}