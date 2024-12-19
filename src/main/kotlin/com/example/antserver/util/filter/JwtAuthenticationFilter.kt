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
        val excludedPaths = listOf("/h2-console/**", "/health", "/users/auth", "/auth/refresh")
        if (excludedPaths.any { request.servletPath.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }
        if (checkAccessToken(request, response, filterChain)) {
            filterChain.doFilter(request, response)
        }
    }

    fun checkAccessToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ): Boolean {
        val accessToken = try {
            authService.getAccessToken(request).takeIf(authService::isTokenValid)
                ?: throw AuthenticationException("Access token이 만료되었습니다.")
        } catch (ex: AuthenticationException) {
            request.setAttribute("customAuthErrorMessage", ex.message)
            throw ex
        }

        authService.parseClaims(accessToken)
            .let { UUID.fromString(it) }
            ?.let { userId ->
                val user = userRepository.findById(userId)
                val userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user?.email)
                    .password("")
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