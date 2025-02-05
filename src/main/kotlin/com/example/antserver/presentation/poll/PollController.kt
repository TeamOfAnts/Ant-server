package com.example.antserver.presentation.poll

import com.example.antserver.application.poll.PollFacade
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.presentation.poll.dto.PageResponse
import com.example.antserver.presentation.poll.dto.PollResponse
import com.example.antserver.util.response.CommonResponse
import com.example.antserver.util.security.jwt.JwtTokenManager
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/polls")
class PollController(
    private val pollFacade: PollFacade,
    private val jwtTokenManager: JwtTokenManager,
) {
    @GetMapping
    fun findPolls(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam status: PollStatus,
        request: HttpServletRequest
    ): CommonResponse<PageResponse<PollResponse>> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val polls = pollFacade.findPollsWithVotedSchedules(status, page, size, userId)
        return CommonResponse(PageResponse.of(polls))
    }
}
