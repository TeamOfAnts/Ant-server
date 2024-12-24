package com.example.antserver.util.jwt

import com.example.antserver.application.auth.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

import com.example.antserver.domain.user.UserRepository
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.response.Status
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Component
class JwtAuthenticationFilter(
    private val jwtTokenManager: JwtTokenManager,
    private val tokenService: TokenService,
    private val userRepository: UserRepository
): OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludedPaths = listOf("/health", "/users/auth", "/auth/refresh")
        return excludedPaths.any { request.servletPath.startsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {

        try {
            val accessToken = jwtTokenManager.getAccessToken(request)
            tokenService.isTokenValid(accessToken)
            authenticateUser(accessToken)
        } catch (exception: ApplicationException) {
            // NOTE: access token 만료 에러의 경우 refresh를 해야하기 때문에 throw한다.
            if (exception.status == Status.Unauthorized) {
                throw exception
            }
        } finally {
            filterChain.doFilter(request, response)
        }
    }

    fun authenticateUser(accessToken: String) {
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val user = userRepository.findById(userId)
            ?: throw ApplicationException(Status.Unauthorized, "Request from an unknown user (userId: $userId).", "인증 오류입니다.")
        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password("")
            .roles(user.role.toString())
            .build()

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }
}