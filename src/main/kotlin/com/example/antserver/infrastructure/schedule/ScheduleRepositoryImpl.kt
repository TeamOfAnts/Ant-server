package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleRepository
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(
    private val jpaScheduleRepository: JpaScheduleRepository,
): ScheduleRepository {

    override fun save(schedule: Schedule): Schedule {
        return jpaScheduleRepository.save(schedule)
    }

    override fun saveAll(schedules: List<Schedule>): List<Schedule> {
        return jpaScheduleRepository.saveAll(schedules)
    }

    override fun findByPollId(pollId: Long): List<Schedule> {
        return jpaScheduleRepository.findByPollId(pollId)
    }
}