package com.example.antserver.domain.schedule

import com.example.antserver.domain.AggregateRoot
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "schedule")
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long ?= null,

    @Column(name = "poll_id")
    val pollId: Long,

    @Column(name = "schedule_on")
    val scheduleOn: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val scheduleStatus: ScheduleStatus
): AggregateRoot() {

    companion object {

        fun of(pollId: Long,
               scheduleOn: Instant,
               scheduleStatus: ScheduleStatus): Schedule {
            return Schedule(
                pollId = pollId,
                scheduleOn = scheduleOn,
                scheduleStatus = scheduleStatus)
        }
    }
}