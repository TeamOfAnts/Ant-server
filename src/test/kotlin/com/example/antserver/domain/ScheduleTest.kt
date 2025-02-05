package com.example.antserver.domain

import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.schedule.ScheduleDescription
import com.example.antserver.domain.schedule.ScheduleOn
import com.example.antserver.domain.schedule.ScheduleStatus
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class ScheduleTest {

    @Test
    @DisplayName("schedules를 생성한다")
    fun createSchedule() {
        // given
        val pollId = 1L
        val startDate = LocalDate.of(2025, 1, 27)
        val startDayOfWeek = startDate.dayOfWeek
        val daysWithUnscheduled = (ChronoUnit.DAYS.between(startDate, startDate.plusDays(13)) + 1) + 1

        // when
        val schedules = Schedule.ofSchedules(pollId, startDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(), daysWithUnscheduled)

        // then
        assertThat(schedules.size).isEqualTo(19)
        assertThat(schedules[0].scheduleOn).isEqualTo(ScheduleOn.Scheduled(startDate))
        assertThat(schedules[0].description).isEqualTo(ScheduleDescription.Scheduled(startDate, startDayOfWeek))
        assertThat(schedules[5].scheduleOn).isEqualTo(ScheduleOn.Scheduled(startDate.plusDays(5)))
        assertThat(schedules[5].description).isEqualTo(ScheduleDescription.Scheduled(startDate.plusDays(5), startDayOfWeek.plus(5), "낮"))
        assertThat(schedules[18].scheduleOn).isEqualTo(ScheduleOn.Unscheduled)
        assertThat(schedules[18].description).isEqualTo(ScheduleDescription.Unscheduled)
    }

    @Test
    @DisplayName("voter를 추가한다")
    fun addVoter() {
        // given
        val voter = User.of("test", "test@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val pollId = 1L
        val startDate = LocalDate.of(2025, 1, 27)
        val daysWithUnscheduled = (ChronoUnit.DAYS.between(startDate, startDate.plusDays(13)) + 1) + 1
        val schedules = Schedule.ofSchedules(pollId, Instant.now(), daysWithUnscheduled)
        var selectedSchedule = schedules.get(1)

        // when
        selectedSchedule = selectedSchedule.addVoter(voter)

        // then
        assertThat(selectedSchedule.voters.get(voter.id)).isEqualTo(voter.name)
    }

    @Test
    @DisplayName("voter를 삭제한다")
    fun deleteVoter() {
        // given
        val voter = User.of("test", "test@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val pollId = 1L
        val startDate = LocalDate.of(2025, 1, 27)
        val daysWithUnscheduled = (ChronoUnit.DAYS.between(startDate, startDate.plusDays(13)) + 1) + 1
        val schedules = Schedule.ofSchedules(pollId, Instant.now(), daysWithUnscheduled)
        var selectedSchedule = schedules.get(1)
        selectedSchedule = selectedSchedule.addVoter(voter)

        // when
        selectedSchedule = selectedSchedule.deleteVoter(voter)

        // then
        assertThat(selectedSchedule.voters).isEmpty()
    }

    @Test
    @DisplayName("voter가 3명 이상인 경우 상태를 CONFIRMED로 업데이트한다")
    fun updateStatusToCONFIRMEDWhenVotersAbove3() {
        // given
        val voter1 = User.of("test1", "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val voter2 = User.of("test2", "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val voter3 = User.of("test3", "test3@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val pollId = 1L
        val startDate = LocalDate.of(2025, 1, 27)
        val daysWithUnscheduled = (ChronoUnit.DAYS.between(startDate, startDate.plusDays(13)) + 1) + 1
        val schedules = Schedule.ofSchedules(pollId, Instant.now(), daysWithUnscheduled)
        var selectedSchedule = schedules.get(1)
        selectedSchedule = selectedSchedule.addVoter(voter1)
        selectedSchedule = selectedSchedule.addVoter(voter2)
        selectedSchedule = selectedSchedule.addVoter(voter3)

        // when
        val updatedSchedule = selectedSchedule.updateStatus()

        // then
        assertThat(updatedSchedule.scheduleStatus).isEqualTo(ScheduleStatus.CONFIRMED)
    }

    @Test
    @DisplayName("voter가 3명 미만인 경우 상태를 DROPPEDD로 업데이트한다")
    fun updateStatusToDROPPEDWhenVotersBelow3() {
        // given
        val voter = User.of("test", "test@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val pollId = 1L
        val voteStartDate = LocalDate.now()
        val scheduleStartDate = voteStartDate.plusDays(4)
        val scheduleEndDate = voteStartDate.plusDays(17)
        val daysWithUnscheduled = (ChronoUnit.DAYS.between(scheduleStartDate, scheduleEndDate.plusDays(13)) + 1) + 1
        val schedules = Schedule.ofSchedules(pollId, Instant.now(), daysWithUnscheduled)
        var selectedSchedule = schedules.get(1)
        selectedSchedule = selectedSchedule.addVoter(voter)

        // when
        val updatedSchedule = selectedSchedule.updateStatus()

        // then
        assertThat(updatedSchedule.scheduleStatus).isEqualTo(ScheduleStatus.DROPPED)
    }
}