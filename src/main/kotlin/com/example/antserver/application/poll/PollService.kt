package com.example.antserver.application.poll

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class PollService(
    private val pollRepository: PollRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun generatePoll(): Poll {
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1).toLocalDate()
        val endDate = startDate.plusDays(13)
        val zoneId = ZoneId.systemDefault()

        val poll = Poll.of(
            title = "모각코 일정 투표",
            description = "${startDate}-${endDate}",
            startAt = startDate.atStartOfDay(zoneId).toInstant(),
            endAt = endDate.atStartOfDay(zoneId).toInstant(),
            pollStatus = PollStatus.OPEN
        )
        pollRepository.save(poll)
        applicationEventPublisher.publishEvent(PollGeneratedEvent(poll.id!!, poll.startAt, poll.endAt))
        return poll
    }

    fun findPollsByStatus(status: PollStatus, page: Int, size: Int): Page<Poll> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return pollRepository.findAllByPollStatus(status, pageable)
    }
}