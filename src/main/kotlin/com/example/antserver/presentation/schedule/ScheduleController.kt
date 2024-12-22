package com.example.antserver.presentation.schedule

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.presentation.schedule.dto.ScheduleRequest
import com.example.antserver.presentation.schedule.dto.ScheduleResponse
import com.example.antserver.presentation.schedule.dto.VoteRequest
import com.example.antserver.presentation.schedule.dto.VoteResponse
import com.example.antserver.util.jwt.JwtTokenManager
import com.example.antserver.util.response.CommonResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val jwtTokenManager: JwtTokenManager
) {

    @GetMapping
    fun findSchedulesByPollId(
        @RequestBody scheduleRequest: ScheduleRequest
    ): CommonResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.findSchedulesByPollId(scheduleRequest.pollId)
        return CommonResponse(schedules.map { ScheduleResponse.from(it) })
    }

    @PatchMapping("/votes")
    fun voteSchedules(
        request: HttpServletRequest,
        @RequestBody voteRequest: VoteRequest
    ): CommonResponse<List<VoteResponse>> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val voteResult = scheduleService.voteSchedules(userId, voteRequest.scheduleIds)
        return CommonResponse(VoteResponse.from(voteResult))
    }
}