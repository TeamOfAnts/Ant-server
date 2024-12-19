package com.example.antserver.presentation.schedule.dto

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleStatus
import java.time.Instant

data class ScheduleResponse(
    val id: Long,
    val pollId: Long,
    val scheduleOn: Instant,
    val scheduleStatus: ScheduleStatus
) {
    companion object {
        fun from(schedule: Schedule): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id!!,
                pollId = schedule.pollId,
                scheduleOn = schedule.scheduleOn,
                scheduleStatus = schedule.scheduleStatus
            )
        }
    }
}