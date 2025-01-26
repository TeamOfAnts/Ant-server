package com.example.antserver.infrastructure.poll

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class PollRepositoryImpl(
    private val jpaPollRepository: JpaPollRepository
): PollRepository {

    override fun save(poll: Poll): Poll {
        return jpaPollRepository.save(poll)
    }

    override fun findById(id: Long): Poll? {
        return jpaPollRepository.findById(id).orElse(null)
    }

    override fun findAllByPollStatus(status: PollStatus, pageable: Pageable): Page<Poll> {
        return jpaPollRepository.findAllByPollStatus(status, pageable)
    }

    override fun findLast(): Poll? {
        return jpaPollRepository.findFirstByOrderByCreatedAtDesc().orElse(null)
    }
}