package com.example.antserver.domain.participation

import java.util.*

interface ParticipationRepository {
    fun save(participation: Participation): Participation
    fun saveAll(participations: List<Participation>): List<Participation>
    fun findAllByUserId(userId: UUID): List<Participation>
}