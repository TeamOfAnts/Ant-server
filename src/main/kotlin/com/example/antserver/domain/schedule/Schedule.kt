package com.example.antserver.domain.schedule

import com.example.antserver.domain.AggregateRoot
import com.example.antserver.domain.user.User
import com.example.antserver.infrastructure.schedule.ScheduleOnConverter
import jakarta.persistence.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(name = "schedule")
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long ?= null,

    @Column(name = "poll_id")
    val pollId: Long,

    @Convert(converter = ScheduleOnConverter::class)
    @Column(name = "schedule_on")
    var scheduleOn: ScheduleOn = ScheduleOn.Unscheduled,

    @ElementCollection
    @Column(name = "voters")
    val voters: MutableMap<UUID, String> = mutableMapOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val scheduleStatus: ScheduleStatus = ScheduleStatus.VOTING
): AggregateRoot() {

    companion object {
        fun ofSchedules(pollId: Long, startDate: Instant, daysWithUnscheduled: Long): List<Schedule> {
            val schedules = mutableListOf<Schedule>()
            (0 until daysWithUnscheduled + 1).map { days ->
                val scheduleOn = if (days < daysWithUnscheduled) {
                    ScheduleOn.Scheduled(
                        startDate.plus(1, ChronoUnit.DAYS)
                    )
                } else {
                    ScheduleOn.Unscheduled
                }
                schedules.add(
                    Schedule(
                        pollId = pollId,
                        scheduleOn = scheduleOn
                    )
                )
            }
            return schedules
        }
    }

    fun updateStatus(): Schedule {
        return copy(scheduleStatus =
            if (voters.size >= 3) ScheduleStatus.CONFIRMED
            else ScheduleStatus.DROPPED)
    }

    fun addVoter(voter: User): Schedule {
        return copy(voters = voters.toMutableMap().apply {
            put(voter.id, voter.name)
        })
    }

    fun deleteVoter(voter: User): Schedule {
        return copy(voters = voters.toMutableMap().apply {
            remove(voter.id)
        })
    }
}

@Embeddable
sealed class ScheduleOn {
    data class Scheduled(val schedule: Instant) : ScheduleOn()
    data object Unscheduled : ScheduleOn()
}