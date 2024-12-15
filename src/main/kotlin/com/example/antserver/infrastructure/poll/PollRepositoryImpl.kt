package com.example.antserver.infrastructure.poll

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollRepository
import com.example.antserver.domain.poll.PollStatus
import jakarta.persistence.NoResultException
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

    override fun findAllByPollStatus(status: PollStatus, pageable: Pageable): Page<Poll> {
        return jpaPollRepository.findAllByPollStatus(status, pageable)
    }

    override fun findLast(): Poll {
        return jpaPollRepository.findFirstByOrderByCreatedAtDesc()
            ?: throw NoResultException("Poll이 없습니다.")
    }
}