package com.example.antserver.domain.participation

import com.example.antserver.domain.AggregateRoot
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

    @Column(name = "participation_type")
    val participationType: ParticipationType
): AggregateRoot() {

    companion object {

        fun of(userId: UUID,
               scheduleId: Long,
               participationType:
               ParticipationType): Participation {
            return Participation(
                userId = userId,
                scheduleId = scheduleId,
                participationType = participationType
            )
        }
    }
}