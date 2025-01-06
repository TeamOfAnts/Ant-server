package com.example.antserver.presentation.schedule

import com.example.antserver.application.schedule.ScheduleService
import com.example.antserver.presentation.schedule.dto.ScheduleResponse
import com.example.antserver.presentation.schedule.dto.VoteRequest
import com.example.antserver.util.response.CommonResponse
import com.example.antserver.util.security.jwt.JwtTokenManager
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
        @RequestParam pollId: Long,
    ): CommonResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.findSchedulesByPollId(pollId)
        return CommonResponse(schedules.map { ScheduleResponse.from(it) })
    }

    @PatchMapping("/votes")
    fun voteSchedules(
        request: HttpServletRequest,
        @RequestBody voteRequest: VoteRequest
    ): CommonResponse<String> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        scheduleService.voteSchedules(userId, voteRequest.scheduleIds)
        return CommonResponse("${voteRequest.scheduleIds}번 스케쥴에 투표했습니다.")
    }
}