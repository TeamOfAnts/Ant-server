package com.example.antserver.presentation.schedule.dto

data class VoteResponse(
    val scheduleId: Long,
    val votes: Int
) {
    companion object {
        fun from(scheduleVotes: Map<Long, Int>): List<VoteResponse> {
            return scheduleVotes.map { (scheduleId, votes) ->
                VoteResponse(scheduleId = scheduleId, votes = votes)
            }
        }
    }
}