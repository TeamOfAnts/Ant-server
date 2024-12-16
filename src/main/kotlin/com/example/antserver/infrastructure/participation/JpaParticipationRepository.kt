package com.example.antserver.infrastructure.participation

import com.example.antserver.domain.participation.Participation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface JpaParticipationRepository: JpaRepository<Participation, Long> {
    fun save(participation: Participation): Participation
    fun saveAll(participations: List<Participation>): List<Participation>
    fun findAllByUserId(userId: UUID): List<Participation>
}