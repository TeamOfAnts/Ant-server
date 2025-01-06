package com.example.antserver.presentation.poll.dto

import com.example.antserver.domain.poll.Poll
import com.example.antserver.domain.poll.PollStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PollResponse(
    val id: Long,
    val title: String,
    val description: String,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val status: PollStatus
) {
    companion object {
        fun of(poll: Poll): PollResponse {
            val zoneId = ZoneId.of("Asia/Seoul")
            return PollResponse(
                id = poll.id!!,
                title = poll.title,
                description = poll.description,
                startAt = poll.startAt.atZone(zoneId).toLocalDate(),
                endAt = poll.endAt.atZone(zoneId).toLocalDate(),
                status = poll.pollStatus
            )
        }
    }
}