package com.example.antserver.application.schedule

import com.example.antserver.application.user.UserService
import com.example.antserver.application.poll.PollGeneratedEvent
import com.example.antserver.domain.schedule.*
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val userService: UserService,
    ) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun generateSchedules(event: PollGeneratedEvent): List<Schedule> {
        val startDate = event.scheduleStartAt
        val endDate = event.scheduleEndAt
        val daysWithUnscheduled = ChronoUnit.DAYS.between(startDate, endDate) + 1

        val schedules = Schedule.ofSchedules(event.pollId, startDate, daysWithUnscheduled)

        return scheduleRepository.saveAll(schedules)
    }

    @Transactional
    fun updateScheduleStatus(pollId: Long) {
        val schedules = scheduleRepository.findAllByPollId(pollId)

        val updatedSchedules = schedules.map { schedule ->
            schedule.updateStatus()
        }
        scheduleRepository.saveAll(updatedSchedules)

        val confirmedSchedules = updatedSchedules.filter { it.scheduleStatus == ScheduleStatus.CONFIRMED }
        applicationEventPublisher.publishEvent(ScheduleConfirmedEvent.of(confirmedSchedules))
    }

    @Transactional
    fun voteSchedules(userId: UUID, selectedScheduleIds: List<Long>): List<Schedule> {
        val voter = userService.findUser(userId)
        val pollId = findPollIdByScheduleId(selectedScheduleIds.first())
        val totalSchedules = findSchedulesByPollId(pollId)
        val updatedSchedules = totalSchedules.map { schedule ->
            when {
                schedule.id !in selectedScheduleIds && voter.id in schedule.voters -> schedule.deleteVoter(voter)
                schedule.id in selectedScheduleIds && voter.id !in schedule.voters -> schedule.addVoter(voter)
                else -> schedule
            }
        }

        return scheduleRepository.saveAll(updatedSchedules)
    }

    fun findSchedulesByPollId(pollId: Long): List<Schedule> {
        return scheduleRepository.findAllByPollId(pollId)
            .ifEmpty {
                throw ApplicationException(
                    Status.BadRequest,
                    "There is no schedule for poll number ${pollId}.",
                    "${pollId}번 투표에 대한 스케쥴이 없습니다."
                )
            }
    }

    fun findPollIdByScheduleId(scheduleId: Long): Long {
        return scheduleRepository.findById(scheduleId)
            ?.pollId
            ?: throw ApplicationException(
                    Status.BadRequest,
                    "Non-existent schedule id(s) ($scheduleId).",
                    "존재하지 않는 스케쥴 id(s)($scheduleId)입니다."
            )
    }
}