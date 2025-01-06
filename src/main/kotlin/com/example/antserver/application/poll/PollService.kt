package com.example.antserver.application.poll

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
class PollService(
    private val pollRepository: PollRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun generatePoll(): Poll {
        val pollStartDate = LocalDate.now() // 일
        val pollEndDate = pollStartDate.plusDays(2) // 화
        val voteStartDate = pollStartDate.plusDays(1) // 월
        val voteEndDate = pollStartDate.plusDays(14) // 일
        val zoneId = ZoneId.systemDefault()

        val poll = Poll.of(
            title = "투표 기한: ${pollEndDate}",
            description = "모각코 예정 기간: ${voteStartDate} ~ ${voteEndDate}",
            startAt = pollStartDate.atStartOfDay(zoneId).toInstant(),
            endAt = pollEndDate.atStartOfDay(zoneId).toInstant(),
            pollStatus = PollStatus.OPEN
        )
        pollRepository.save(poll)
        applicationEventPublisher.publishEvent(
            PollGeneratedEvent.of(
                poll.id!!,
                voteStartDate.atStartOfDay(zoneId).toInstant(),
                voteEndDate.atStartOfDay(zoneId).toInstant()
            )
        )
        return poll
    }

    fun findPollsByStatus(status: PollStatus, page: Int, size: Int): Page<Poll> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return pollRepository.findAllByPollStatus(status, pageable)
    }

    fun updatePollStatus(pollId: Long, status: PollStatus) {
        val poll = pollRepository.findById(pollId)
            ?: throw ApplicationException(Status.ServerError, "poll with id $pollId does not exist", "")
        poll.updateStatus(status)
        pollRepository.save(poll)
    }
}