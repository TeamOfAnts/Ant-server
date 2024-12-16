package com.example.antserver.domain.poll

import java.time.Instant

data class PollGeneratedEvent(
    val pollId: Long,
    val startAt: Instant,
    val endAt: Instant
)