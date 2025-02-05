package com.example.antserver.application.poll

import java.time.Instant

data class PollGeneratedEvent(
    val pollId: Long,
    val scheduleStartAt: Instant,
    val scheduleEndAt: Instant
) {
    companion object {
        fun of(pollId: Long,
               scheduleStartAt: Instant,
               scheduleEndAt: Instant): PollGeneratedEvent {
            return PollGeneratedEvent(pollId, scheduleStartAt, scheduleEndAt)
        }
    }
}