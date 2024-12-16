package com.example.antserver.application.schedule

import java.util.*

data class ScheduleStatusChangedEvent(
    val confirmedScheduleVoters: Map<Long, List<UUID>>
) {
    companion object {
        fun from(
            confirmedScheduleVoters: Map<Long, List<UUID>>):
                ScheduleStatusChangedEvent {
            return ScheduleStatusChangedEvent(
                confirmedScheduleVoters = confirmedScheduleVoters)
        }
    }
}
