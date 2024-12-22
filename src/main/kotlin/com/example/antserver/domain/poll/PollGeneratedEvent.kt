package com.example.antserver.domain.poll

import java.time.Instant

data class PollGeneratedEvent(
    val pollId: Long,
    val startAt: Instant,
    val endAt: Instant
) {
    companion object {
        fun of(pollId: Long,
               startAt: Instant,
               endAt: Instant): PollGeneratedEvent {
            return PollGeneratedEvent(pollId, startAt, endAt)
        }
    }
}