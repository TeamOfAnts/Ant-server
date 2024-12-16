package com.example.antserver.domain.poll

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PollRepository {
    fun save(poll: Poll): Poll
    fun findAllByPollStatus(status: PollStatus, pageable: Pageable): Page<Poll>
    fun findLast(): Poll
}