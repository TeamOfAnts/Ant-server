package com.example.antserver.domain.participation

import java.util.*

interface ParticipationRepository {
    fun save(participation: Participation): Participation
    fun findAllByUserId(userId: UUID): List<Participation>
}