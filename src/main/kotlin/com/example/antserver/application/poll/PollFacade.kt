package com.example.antserver.application.poll

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.presentation.poll.dto.PollResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import java.util.*

@Service
class PollFacade(
    private val pollService: PollService,
    private val scheduleService: ScheduleService
) {

    fun findPollsWithVotedSchedules(status: PollStatus, page: Int, size: Int, userId: UUID): Page<PollResponse> {
        val polls = pollService.findPollsByStatus(status, page, size)
        val pollsWithVotedSchedules = polls.content.map { poll ->
            val schedules = scheduleService.findSchedulesByPollId(poll.id!!)
            val votedSchedules = schedules
                .filter { schedule -> userId in schedule.voters }
                .mapNotNull { it.id }
            PollResponse.of(poll, votedSchedules)
        }

        return PageImpl(pollsWithVotedSchedules, polls.pageable, polls.totalElements)
    }
}