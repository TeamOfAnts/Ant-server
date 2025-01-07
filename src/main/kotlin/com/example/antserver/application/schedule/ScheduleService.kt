package com.example.antserver.application.schedule

import com.example.antserver.domain.poll.PollGeneratedEvent
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleRepository
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
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
        val startDate = event.voteStartAt
        val nextDayOfEndDate = event.voteEndAt.plus(1, ChronoUnit.DAYS)
        val schedules = (0L until ChronoUnit.DAYS.between(startDate, nextDayOfEndDate))
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

        val confirmedSchedules = updatedSchedules.filter { it.scheduleStatus == ScheduleStatus.CONFIRMED }
        applicationEventPublisher.publishEvent(ScheduleConfirmedEvent.of(confirmedSchedules))
    }

    fun findSchedulesByPollId(pollId: Long): List<Schedule> {
        return scheduleRepository.findAllByPollId(pollId)
            .takeIf { it.isNotEmpty() } ?: throw ApplicationException(Status.BadRequest, "There is no schedule for poll number ${pollId}.", "${pollId}번 투표에 대한 스케쥴이 없습니다.")
    }

    @Transactional
    fun voteSchedules(userId: UUID, scheduleIds: List<Long>): List<Schedule> {
        val votingSchedules = scheduleRepository.findAllById(scheduleIds)
        val pollId = votingSchedules.first().pollId
        val totalSchedules = scheduleRepository.findAllByPollId(pollId)
        val votedScheduleIds = totalSchedules.filter { userId in it.voters }.map { it.id }.toSet()

        val voterRemovedSchedules = totalSchedules.filter { it.id in votedScheduleIds && it.id !in scheduleIds }
        voterRemovedSchedules.forEach { it.deleteVoter(userId) }

        val voterAddedSchedules = totalSchedules.filter { it.id !in votedScheduleIds && it.id in scheduleIds }
        voterAddedSchedules.forEach { it.addVoter(userId) }

        return scheduleRepository.saveAll(voterRemovedSchedules + voterAddedSchedules)
    }
}