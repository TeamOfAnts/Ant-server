package com.example.antserver.application.schedule

import com.example.antserver.domain.schedule.Schedule
import java.util.*

data class ScheduleStatusChangedEvent(
    val confirmedScheduleVoters: List<Schedule>
) {
    companion object {
        fun from(
            confirmedScheduleVoters: List<Schedule>):
                ScheduleStatusChangedEvent {
            return ScheduleStatusChangedEvent(
                confirmedScheduleVoters = confirmedScheduleVoters)
        }
    }
}
