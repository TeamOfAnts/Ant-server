package com.example.antserver.presentation.poll.dto

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import java.time.Instant

data class PollResponse(
    val id: Long,
    val title: String,
    val description: String,
    val startAt: Instant,
    val endAt: Instant,
    val status: PollStatus
) {
    companion object {
        fun of(poll: Poll): PollResponse {
            return PollResponse(
                id = poll.id!!,
                title = poll.title,
                description = poll.description,
                startAt = poll.startAt,
                endAt = poll.endAt,
                status = poll.pollStatus
            )
        }
    }
}