package com.example.antserver.domain

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class PollTest {

    @Test
    @DisplayName("poll 생성")
    fun createPoll() {
        // given
        val pollId = 1L
        val voteStartAt = Instant.now()
        val voteEndAt = voteStartAt.plus(2, ChronoUnit.DAYS)
        val scheduleStartAt = voteStartAt.plus(4, ChronoUnit.DAYS)
        val scheduleEndAt = voteStartAt.plus(17, ChronoUnit.DAYS)

        // when
        val poll = Poll.of(
            id = 1L,
            voteStartAt = voteStartAt,
            voteEndAt = voteEndAt,
            scheduleStartAt = scheduleStartAt,
            scheduleEndAt = scheduleEndAt
        )

        // then
        assertThat(poll.id).isEqualTo(pollId)
        assertThat(poll.startAt).isEqualTo(voteStartAt)
        assertThat(poll.endAt).isEqualTo(voteEndAt)
        assertThat(poll.pollStatus).isEqualTo(PollStatus.OPEN)
    }
}