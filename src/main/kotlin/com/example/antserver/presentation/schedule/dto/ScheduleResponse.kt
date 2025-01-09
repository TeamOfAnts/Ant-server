package com.example.antserver.presentation.schedule.dto

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class ScheduleResponse(
    val id: Long,
    val pollId: Long,
    val scheduleOn: LocalDate,
    val voters: List<String>,
    val scheduleStatus: ScheduleStatus
) {
    companion object {
        fun of(schedule: Schedule): ScheduleResponse {
            val zoneId = ZoneId.of("Asia/Seoul")
            return ScheduleResponse(
                id = schedule.id!!,
                pollId = schedule.pollId,
                scheduleOn = schedule.scheduleOn.atZone(zoneId).toLocalDate(),
                voters = schedule.voters.map { voter -> voter.value},
                scheduleStatus = schedule.scheduleStatus
            )
        }
    }
}