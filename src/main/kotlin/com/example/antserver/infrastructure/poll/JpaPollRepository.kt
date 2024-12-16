package com.example.antserver.infrastructure.poll

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface JpaPollRepository: JpaRepository<Poll, Long> {
    fun save(poll: Poll): Poll
    fun findAllByPollStatus(status: PollStatus, pageable: Pageable): Page<Poll>
    fun findFirstByOrderByCreatedAtDesc(): Poll?
}