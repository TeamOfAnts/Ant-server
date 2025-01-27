package com.example.antserver.application

import com.example.antserver.application.poll.PollService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.fake.FakePollRepository
import com.example.antserver.testconfig.TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@Import(TestConfig::class)
@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PollServiceTest {

    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var pollRepository: FakePollRepository

    private lateinit var poll: Poll

    @BeforeEach
    fun set() {
        poll = Poll.of(
            id = 1L,
            voteStartAt = Instant.now(),
            voteEndAt = Instant.now().plus(2, ChronoUnit.DAYS),
            scheduleStartAt = Instant.now().plus(4, ChronoUnit.DAYS),
            scheduleEndAt = Instant.now().plus(17, ChronoUnit.DAYS)
        )
        pollRepository.save(poll)
    }

    @AfterEach
    fun clear() {
        pollRepository.clear()
    }

    @Test
    @DisplayName("poll을 생성하면 OPEN 상태이다")
    fun generatePoll() {
        // given & when
        val poll = poll

        // then
        assertThat(poll.pollStatus).isEqualTo(PollStatus.OPEN)
    }

    @Test
    @DisplayName("poll을 종료하면 CLOSED 상태이다")
    fun endPoll() {
        // given
        val poll = poll

        // when
        pollService.updatePollStatus(poll.id!!, PollStatus.CLOSED)
        val updatedPoll = pollRepository.findById(poll.id!!)

        // then
        assertThat(updatedPoll?.pollStatus).isEqualTo(PollStatus.CLOSED)
    }

    @Test
    @DisplayName("OPEN 상태의 poll을 조회한다")
    fun getOpenPoll() {
        // given
        val poll = poll

        // when
        val openPolls = pollService.findPollsByStatus(PollStatus.OPEN, 0, 10)

        // then
        assertThat(openPolls.totalElements).isEqualTo(1)
        assertThat(openPolls.size).isEqualTo(10)
    }

    @Test
    @DisplayName("CLOSED 상태의 poll을 조회한다")
    fun getClosedPolls() {
        // given
        val poll1 = poll
        val poll2 = Poll.of(
            id = 2L,
            voteStartAt = Instant.now(),
            voteEndAt = Instant.now().plus(2, ChronoUnit.DAYS),
            scheduleStartAt = Instant.now().plus(4, ChronoUnit.DAYS),
            scheduleEndAt = Instant.now().plus(17, ChronoUnit.DAYS)
            )
        pollRepository.save(poll2)
        val poll3 = Poll.of(
            id = 1L,
            voteStartAt = Instant.now(),
            voteEndAt = Instant.now().plus(2, ChronoUnit.DAYS),
            scheduleStartAt = Instant.now().plus(4, ChronoUnit.DAYS),
            scheduleEndAt = Instant.now().plus(17, ChronoUnit.DAYS)
            )
        pollRepository.save(poll3)

        // when
        pollService.updatePollStatus(poll1.id!!, PollStatus.CLOSED)
        pollService.updatePollStatus(poll2.id!!, PollStatus.CLOSED)
        val closedPolls = pollService.findPollsByStatus(PollStatus.CLOSED, 0, 10)

        // then
        assertThat(closedPolls.totalElements).isEqualTo(2)
        assertThat(closedPolls.size).isEqualTo(10)
    }
}
