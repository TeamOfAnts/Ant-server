package com.example.antserver.presentation.schedule.dto

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleOn
import com.example.antserver.domain.schedule.ScheduleStatus
import java.time.ZoneId

data class ScheduleResponse(
    val id: Long,
    val pollId: Long,
    val scheduleOn: Any,
    val voters: List<String>,
    val scheduleStatus: ScheduleStatus
) {
    companion object {
        fun of(schedule: Schedule): ScheduleResponse {
            val zoneId = ZoneId.of("Asia/Seoul")
            val scheduleOn = when (val localDate = schedule.scheduleOn) {
                is ScheduleOn.Scheduled -> localDate.schedule.atZone(zoneId).toLocalDate()
                is ScheduleOn.Unscheduled -> "미참여"
            }
            return ScheduleResponse(
                id = schedule.id!!,
                pollId = schedule.pollId,
                scheduleOn = scheduleOn,
                voters = schedule.voters.map { voter -> voter.value},
                scheduleStatus = schedule.scheduleStatus
            )
        }
    }
}
