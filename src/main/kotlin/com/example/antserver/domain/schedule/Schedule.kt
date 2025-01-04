package com.example.antserver.domain.schedule

import com.example.antserver.domain.AggregateRoot
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
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
    val voters: MutableSet<UUID>,

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
                voters = HashSet<UUID>(),
                scheduleStatus = scheduleStatus)
        }
    }

    fun addVoter(userId: UUID) {
        require (userId !in voters) {
            throw ApplicationException(Status.BadRequest, "$userId attempted to vote multiple times.", "중복 투표는 불가능합니다.")
        }
        voters.add(userId)
    }
}