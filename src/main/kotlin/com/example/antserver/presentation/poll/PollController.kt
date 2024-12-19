package com.example.antserver.presentation.poll

import com.example.antserver.application.poll.PollService
import com.example.antserver.domain.poll.PollStatus
import com.example.antserver.presentation.poll.dto.PageResponse
import com.example.antserver.presentation.poll.dto.PollResponse
import com.example.antserver.util.response.CommonResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/polls")
class PollController(
    private val pollService: PollService,
) {
    @GetMapping
    fun findPolls(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam status: PollStatus
    ): CommonResponse<PageResponse<PollResponse>> {
        val polls = pollService.findPollsByStatus(status, page, size)
        return CommonResponse(
            PageResponse.from(
                polls.map { PollResponse.of(it) }
            )
        )
    }
}
