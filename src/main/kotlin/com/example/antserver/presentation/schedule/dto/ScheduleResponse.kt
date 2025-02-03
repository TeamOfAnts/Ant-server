package com.example.antserver.presentation.schedule.dto

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleDescription
import com.example.antserver.domain.schedule.ScheduleOn
import com.example.antserver.domain.schedule.ScheduleStatus
import java.time.LocalDate

data class ScheduleResponse(
    val id: Long,
    val pollId: Long,
    val scheduleOn: LocalDate?,
    val description: String,
    val voters: List<String>,
    val scheduleStatus: ScheduleStatus
) {
    companion object {
        fun of(schedule: Schedule): ScheduleResponse {
            val scheduleOn = when (val localDate = schedule.scheduleOn) {
                is ScheduleOn.Scheduled -> localDate.date
                is ScheduleOn.Unscheduled -> null
            }

            val description = when (val scheduleDescription = schedule.description) {
                is ScheduleDescription.Scheduled -> scheduleDescription.toString()
                is ScheduleDescription.Unscheduled -> "미참여"
            }

            return ScheduleResponse(
                id = schedule.id!!,
                pollId = schedule.pollId,
                scheduleOn = scheduleOn,
                description = description,
                voters = schedule.voters.map { voter -> voter.value},
                scheduleStatus = schedule.scheduleStatus
            )
        }
    }
}
