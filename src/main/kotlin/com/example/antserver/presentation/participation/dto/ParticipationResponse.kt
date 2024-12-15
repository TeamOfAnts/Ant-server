package com.example.antserver.presentation.participation.dto

import com.example.antserver.domain.participation.ParticipationType

data class ParticipationResponse(
    val scheduleId: Long,
    val participationType: ParticipationType,
) {

    companion object {
        fun of(scheduleId: Long,
               participationType: ParticipationType): ParticipationResponse {
            return ParticipationResponse(
                scheduleId = scheduleId,
                participationType = participationType
            )
        }
    }
}