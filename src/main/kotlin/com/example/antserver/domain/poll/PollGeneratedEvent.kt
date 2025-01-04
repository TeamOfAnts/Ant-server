package com.example.antserver.domain.poll

import java.time.Instant

data class PollGeneratedEvent(
    val pollId: Long,
    val voteStartAt: Instant,
    val voteEndAt: Instant
) {
    companion object {
        fun of(pollId: Long,
               voteStartAt: Instant,
               voteEndAt: Instant): PollGeneratedEvent {
            return PollGeneratedEvent(pollId, voteStartAt, voteEndAt)
        }
    }
}