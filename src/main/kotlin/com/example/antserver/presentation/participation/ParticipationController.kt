package com.example.antserver.presentation.participation

import com.example.antserver.application.participation.ParticipationService
import com.example.antserver.presentation.participation.dto.ParticipationResponse
import com.example.antserver.util.response.CommonResponse
import com.example.antserver.util.security.jwt.JwtTokenManager
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/participation")
class ParticipationController(
    private val participationService: ParticipationService,
    private val jwtTokenManager: JwtTokenManager
) {
    @GetMapping
    fun findParticipation(request: HttpServletRequest): CommonResponse<List<ParticipationResponse>> {
        val accessToken = jwtTokenManager.getAccessToken(request)
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val participations = participationService.findParticipation(userId)
        return CommonResponse(participations.map { ParticipationResponse.of(it.scheduleId, it.participationType) })
    }
}