package com.example.antserver.domain.schedule

import com.example.antserver.domain.AggregateRoot
import com.example.antserver.domain.user.User
import jakarta.persistence.*
import java.time.Instant
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

    @Column(name = "schedule_on")
    val scheduleOn: Instant,

    @ElementCollection
    @Column(name = "voters")
    val voters: MutableMap<UUID, String>,

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
                voters = mutableMapOf(),
                scheduleStatus = scheduleStatus)
        }
    }

    fun addVoter(voter: User) {
        voters[voter.id] = voter.name
    }

    fun deleteVoter(voter: User) {
        voters.remove(voter.id)
    }
}