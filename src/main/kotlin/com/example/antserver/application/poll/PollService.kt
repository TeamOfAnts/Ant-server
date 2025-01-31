package com.example.antserver.application.poll

import com.example.antserver.domain.poll.Poll
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
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PollService(
    private val pollRepository: PollRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun generatePoll(): Poll {
        val voteStartAt = Instant.now() // 목
        val voteEndAt = voteStartAt.plus(2, ChronoUnit.DAYS) // 토
        val scheduleStartAt = voteStartAt.plus(4, ChronoUnit.DAYS) // 월
        val scheduleEndAt = voteStartAt.plus(17, ChronoUnit.DAYS) // 일

        val poll = Poll.of(voteStartAt, voteEndAt, scheduleStartAt, scheduleEndAt)
        val savedPoll = pollRepository.save(poll)

        applicationEventPublisher.publishEvent(
            PollGeneratedEvent.of(
                savedPoll.id!!,
                scheduleStartAt,
                scheduleEndAt
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