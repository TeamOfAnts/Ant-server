package com.example.antserver.application.schedule

import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleRepository
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.util.exception.EmptyResultException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    ) {

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

    @Transactional
    fun updateScheduleStatus(pollId: Long) {
        val schedules = scheduleRepository.findAllByPollId(pollId)

        val updatedSchedules = schedules.map { schedule ->
            val newStatus = if (schedule.voters.size >= 3) ScheduleStatus.CONFIRMED else ScheduleStatus.DROPPED
            schedule.copy(scheduleStatus = newStatus)
        }

        scheduleRepository.saveAll(updatedSchedules)
        applicationEventPublisher.publishEvent(ScheduleStatusChangedEvent.from(updatedSchedules))
    }

    fun findSchedulesByPollId(pollId: Long): List<Schedule> {
        return scheduleRepository.findAllByPollId(pollId)
            .takeIf { it.isNotEmpty() } ?: throw EmptyResultException("{$pollId}번 투표에 대한 스케쥴이 없습니다.")
    }

    @Transactional
    fun voteSchedules(userId: UUID, scheduleIds: List<Long>): List<Schedule> {
        val schedules = scheduleRepository.findAllById(scheduleIds)
        schedules.forEach { schedule ->
            schedule.addVoter(userId)
        }

        return scheduleRepository.saveAll(schedules)
    }
}