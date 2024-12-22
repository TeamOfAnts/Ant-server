package com.example.antserver.domain.schedule

interface ScheduleRepository {
    fun save(schedule: Schedule): Schedule
    fun saveAll(schedules: List<Schedule>): List<Schedule>
    fun findAllByPollId(pollId: Long): List<Schedule>
}