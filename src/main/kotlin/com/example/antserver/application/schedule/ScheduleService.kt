package com.example.antserver.application.schedule

import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleRepository
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.util.exception.EmptyResultException
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository
) {
    val scheduleVoters = mutableMapOf<Long, MutableSet<UUID>>()

    @EventListener
    @Transactional
    fun generateSchedules(event: PollGeneratedEvent): List<Schedule> {
        val startDate = event.startAt
        val schedules = (0L until ChronoUnit.DAYS.between(event.startAt, event.endAt))
            .map { days ->
                Schedule.of(
                    pollId = event.pollId,
                    scheduleOn = startDate.plus(days, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant(),
                    scheduleStatus = ScheduleStatus.VOTING,
                )
            }
        return scheduleRepository.saveAll(schedules)
    }

    fun findSchedulesByPollId(pollId: Long): List<Schedule> {
        return scheduleRepository.findByPollId(pollId)
            .takeIf { it.isNotEmpty() } ?: throw EmptyResultException("{$pollId}번 투표에 대한 스케쥴이 없습니다.")

    }

    @Transactional
    fun voteSchedules(userId: UUID, scheduleIds: List<Long>): Map<Long, Int> {
        scheduleIds.forEach { scheduleId ->
            val votes = scheduleVoters.getOrPut(scheduleId) { mutableSetOf() }
            votes.add(userId)
        }

        return scheduleVoters.mapValues { it.value.size }
    }
}