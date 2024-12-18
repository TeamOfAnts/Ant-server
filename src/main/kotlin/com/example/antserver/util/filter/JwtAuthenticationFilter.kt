package com.example.antserver.util.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

import com.example.antserver.application.auth.AuthService
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.util.exception.AuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Component
class JwtAuthenticationFilter(
    private val authService: AuthService,
    private val userRepository: UserRepository
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val excludedPaths = listOf("/users/auth", "/auth/refresh")
        if (excludedPaths.any { request.servletPath.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        when (checkAccessToken(request, response, filterChain)) {
            true -> filterChain.doFilter(request, response)
            false -> throw AuthenticationException("Access token이 만료되었습니다.")
        }
    }

    fun checkAccessToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ): Boolean {
        val accessToken = authService.getAccessToken(request).takeIf(authService::isTokenValid)
            ?: return false

        authService.parseClaims(accessToken)
            .let { UUID.fromString(it) }
            ?.let { userId ->
                val user = userRepository.findById(userId)
                val userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user?.email)
                            .roles(user?.role.toString())
                    .build()

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
                }
            return true
        }
}