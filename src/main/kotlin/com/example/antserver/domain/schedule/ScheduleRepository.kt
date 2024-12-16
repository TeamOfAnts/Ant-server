package com.example.antserver.domain.schedule

interface ScheduleRepository {
    fun save(schedule: Schedule): Schedule
    fun saveAll(schedules: List<Schedule>): List<Schedule>
    fun findByPollId(pollId: Long): List<Schedule>
}