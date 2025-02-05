package com.example.antserver.domain.poll

import com.example.antserver.domain.AggregateRoot
import jakarta.persistence.*
import java.time.Instant
import java.time.ZoneId

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
    var pollStatus: PollStatus,

    ): AggregateRoot() {
        companion object {
            fun of(
                   voteStartAt: Instant,
                   voteEndAt: Instant,
                   scheduleStartAt: Instant,
                   scheduleEndAt: Instant,
                   id: Long? = null): Poll {
                val seoulZoneId = ZoneId.of("Asia/Seoul")
                val voteEndDate = voteEndAt.atZone(seoulZoneId).toLocalDate()
                val scheduleStartAt = scheduleStartAt.atZone(seoulZoneId).toLocalDate()
                val scheduleEndAt = scheduleEndAt.atZone(seoulZoneId).toLocalDate()

                return Poll(
                    id = id,
                    title = "투표 기한: ${voteEndDate} 18시",
                    description = "모각코 예정 기간: ${scheduleStartAt} ~ ${scheduleEndAt}",
                    startAt = voteStartAt,
                    endAt = voteEndAt,
                    pollStatus = PollStatus.OPEN
                )
            }
        }

    fun updateStatus(newStatus: PollStatus) {
        pollStatus = newStatus
    }
}
