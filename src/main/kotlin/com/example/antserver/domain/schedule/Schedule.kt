package com.example.antserver.domain.schedule

import com.example.antserver.domain.AggregateRoot
import com.example.antserver.domain.user.User
import com.example.antserver.infrastructure.schedule.ScheduleOnConverter
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
            val seoulZoneId = ZoneId.of("Asia/Seoul")

            (0 until daysWithUnscheduled).forEach { day ->
                val currentDate = startDate.plus(day, ChronoUnit.DAYS).atZone(seoulZoneId).toLocalDate()
                val dayOfWeek = currentDate.dayOfWeek

                schedules.addAll(
                    when {
                        day < daysWithUnscheduled - 1 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) -> listOf(
                            Schedule(pollId = pollId, scheduleOn = ScheduleOn.Scheduled(currentDate, dayOfWeek, "낮")),
                            Schedule(pollId = pollId, scheduleOn = ScheduleOn.Scheduled(currentDate, dayOfWeek, "저녁"))
                        )
                        day < daysWithUnscheduled - 1 -> listOf(
                            Schedule(pollId = pollId, scheduleOn = ScheduleOn.Scheduled(currentDate, dayOfWeek))
                        )
                        else -> listOf(
                            Schedule(pollId = pollId, scheduleOn = ScheduleOn.Unscheduled)
                        )
                    }
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
    abstract override fun toString(): String

    companion object {
        private val koreanDays = mapOf(
            DayOfWeek.MONDAY to "월",
            DayOfWeek.TUESDAY to "화",
            DayOfWeek.WEDNESDAY to "수",
            DayOfWeek.THURSDAY to "목",
            DayOfWeek.FRIDAY to "금",
            DayOfWeek.SATURDAY to "토",
            DayOfWeek.SUNDAY to "일"
        )
        fun getKoreanDay(dayOfWeek: DayOfWeek): String {
            return koreanDays[dayOfWeek]!!
        }
    }

    data class Scheduled(
        val date: LocalDate,
        val dayOfWeek: DayOfWeek,
        val timeOfDay: String? = null) : ScheduleOn() {
            override fun toString(): String {
                return listOfNotNull(
                    date.toString(),
                    "(${getKoreanDay(dayOfWeek)})",
                    timeOfDay
                ).joinToString(" ")
            }
        }

    data object Unscheduled : ScheduleOn()
}