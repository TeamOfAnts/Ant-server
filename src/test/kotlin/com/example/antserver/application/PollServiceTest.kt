package com.example.antserver.application

import com.example.antserver.application.poll.PollService
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@Transactional
@SpringBootTest
class PollServiceTest {

    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var pollRepository: PollRepository

    @Test
    @DisplayName("poll을 생성하면 OPEN 상태이다")
    fun generatePoll() {
        // given & when
        val poll = pollService.generatePoll()

        // then
        assertThat(poll.pollStatus).isEqualTo(PollStatus.OPEN)
    }

    @Test
    @DisplayName("poll을 종료하면 CLOSED 상태이다")
    fun endPoll() {
        // given
        val poll = pollService.generatePoll()

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
        pollService.generatePoll()

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
        val poll1 = pollService.generatePoll()
        val poll2 = pollService.generatePoll()
        val poll3 = pollService.generatePoll()

        // when
        pollService.updatePollStatus(poll1.id!!, PollStatus.CLOSED)
        pollService.updatePollStatus(poll2.id!!, PollStatus.CLOSED)
        val closedPolls = pollService.findPollsByStatus(PollStatus.CLOSED, 0, 10)

        // then
        assertThat(closedPolls.totalElements).isEqualTo(2)
        assertThat(closedPolls.size).isEqualTo(10)
    }
}
