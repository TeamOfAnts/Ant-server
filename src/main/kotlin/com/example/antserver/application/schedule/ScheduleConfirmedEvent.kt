package com.example.antserver.application.schedule

import com.example.antserver.domain.schedule.Schedule

data class ScheduleConfirmedEvent(
    val confirmedSchedules: List<Schedule>
) {
    companion object {
        fun of(
            confirmedSchedules: List<Schedule>):
                ScheduleConfirmedEvent {
            return ScheduleConfirmedEvent(
                confirmedSchedules = confirmedSchedules)
        }
    }
}
