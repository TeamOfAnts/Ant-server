package com.example.antserver.application

import com.example.antserver.application.poll.PollService
import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test

@SpringBootTest
class PollServiceTest {

    @Autowired
    private lateinit var pollService: PollService

    @Autowired
    private lateinit var pollRepository: PollRepository

    @Test
    @DisplayName("test")
    fun findPollsById() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(3).toLocalDate()
        val endDate = startDate.plusDays(14)
        val zoneId = ZoneId.systemDefault()

        // OPEN 상태의 Poll 생성
        val openPoll = Poll.of(
            title = "모각코 일정 투표 - ${endDate}까지 가능한 날짜를 선택해주세요",
            description = "모각코 예정 기간: ${startDate}-${endDate}",
            startAt = startDate.atStartOfDay(zoneId).toInstant(),
            endAt = endDate.atStartOfDay(zoneId).toInstant(),
            pollStatus = PollStatus.OPEN
        )
        pollRepository.save(openPoll)

        // CLOSED 상태의 Poll 생성
        val closedPoll = Poll.of(
            title = "모각코 일정 투표 - ${endDate}까지 가능한 날짜를 선택해주세요",
            description = "모각코 예정 기간: ${startDate.minusDays(20)}-${startDate.minusDays(6)}",
            startAt = startDate.minusDays(20).atStartOfDay(zoneId).toInstant(),
            endAt = startDate.minusDays(6).atStartOfDay(zoneId).toInstant(),
            pollStatus = PollStatus.CLOSED
        )
        pollRepository.save(closedPoll)

        // when
        val openPolls = pollService.findPollsByStatus(PollStatus.OPEN, 0, 10)
        val closedPolls = pollService.findPollsByStatus(PollStatus.CLOSED, 0, 10)

        // then
        assertThat(openPolls.size).isEqualTo(10)
        assertThat(openPolls.content[0].title).isEqualTo(openPoll.title)

        assertThat(closedPolls.size).isEqualTo(10)
        assertThat(closedPolls.content[0].title).isEqualTo(closedPoll.title)
    }
}
