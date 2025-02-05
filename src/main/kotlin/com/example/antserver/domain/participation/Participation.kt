package com.example.antserver.domain.participation

import com.example.antserver.domain.AggregateRoot
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "participation")
data class Participation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "schedule_id", nullable = false)
    val scheduleId: Long,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val participationType: ParticipationType
): AggregateRoot() {

    companion object {
        fun from(schedule: Schedule): List<Participation> {
            return schedule.voters.map { voter ->
                Participation(
                    scheduleId = schedule.id ?: throw ApplicationException(Status.ServerError, "Schedule id is null", ""),
                    userId = voter.key,
                    participationType = ParticipationType.STUDY
                )
            }
        }
    }
}