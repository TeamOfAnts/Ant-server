package com.example.antserver.domain.poll

import com.example.antserver.domain.AggregateRoot
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "poll")
data class Poll(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long ?= null,

    @Column(name = "title")
    val title: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "start_at")
    val startAt: Instant,

    @Column(name = "end_at")
    val endAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val pollStatus: PollStatus,

    ): AggregateRoot() {
        companion object {
            fun of(title: String,
                   description: String,
                   startAt: Instant,
                   endAt: Instant,
                   pollStatus: PollStatus): Poll {
                return Poll(
                    title = title,
                    description = description,
                    startAt = startAt,
                    endAt = endAt,
                    pollStatus = pollStatus)
            }
        }
}
