package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.Schedule
import org.springframework.data.jpa.repository.JpaRepository

interface JpaScheduleRepository: JpaRepository<Schedule, Long> {
    fun findAllByPollId(pollId: Long): List<Schedule>
}