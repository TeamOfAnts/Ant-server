package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.Schedule
import org.springframework.data.jpa.repository.JpaRepository

interface JpaScheduleRepository: JpaRepository<Schedule, Long> {
    fun findByPollId(pollId: Long): List<Schedule>
}