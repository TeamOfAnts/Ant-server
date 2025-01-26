package com.example.antserver.domain

import com.example.antserver.domain.participation.Participation
import com.example.antserver.domain.schedule.Schedule
import com.example.antserver.domain.user.ProviderType
import com.example.antserver.domain.user.User
import com.example.antserver.domain.user.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ParticipationTest {

    @Test
    @DisplayName("participation을 생성한다")
    fun createParticipation() {
        // given
        val user1 = User.of("test1", "test1@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user2 = User.of("test2", "test2@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)
        val user3 = User.of("test3", "test3@gmail.com", ProviderType.GOOGLE, "", UserRoleType.MEMBER)

        val voteStartDate = LocalDate.now()
        val scheduleStartDate = voteStartDate.plusDays(4)
        val scheduleEndDate = voteStartDate.plusDays(17)
        val daysWithUnscheduled = ChronoUnit.DAYS.between(scheduleStartDate, scheduleEndDate) + 1
        val schedules = Schedule.ofSchedules(1L, Instant.now(), daysWithUnscheduled)

        // 1, 2, 3번 스케쥴은 3표 이상이므로 CONFIRMED, 4, 5, 6, 7번은 DROPPED
        val updatedSchedules = schedules.toMutableList()
        updatedSchedules.forEachIndexed { index, schedule ->
            var updatedSchedule = schedule.copy(id = index + 1L, voters = schedule.voters.toMutableMap())

            if (updatedSchedule.id in listOf(1L, 2L, 3L)) {
                updatedSchedule = updatedSchedule.addVoter(user1)
                updatedSchedule = updatedSchedule.addVoter(user2)
                updatedSchedule = updatedSchedule.addVoter(user3)
            }
            updatedSchedule = updatedSchedule.updateStatus()
            updatedSchedules[index] = updatedSchedule
        }

        // when
        val participations = updatedSchedules
            .filter { it.id in listOf(1L, 2L, 3L) }
            .map { schedule -> Participation.from(schedule) }

        // then
        val participationsWithVoters = participations
            .flatten() // 이중 리스트를 단일 리스트로 변환
            .filter { it.scheduleId in listOf(1L, 2L, 3L) }
        participationsWithVoters.forEach { participation ->
            assertThat(participation.userId).isIn(listOf(user1.id, user2.id, user3.id))
        }

        val participationsWithoutVoters = participations
            .flatten()
            .filterNot { it.scheduleId in listOf(1L, 2L, 3L) }
        assertThat(participationsWithoutVoters).isEmpty()
    }
}

