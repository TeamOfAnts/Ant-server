package com.example.antserver.util.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

import com.example.antserver.domain.user.UserRepository
import com.example.antserver.util.exception.AuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Component
class JwtAuthenticationFilter(
    private val jwtTokenManager: JwtTokenManager,
    private val userRepository: UserRepository
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if ( isExcludedPath(request.servletPath) ) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken = checkAccessToken(request)
        authenticateUser(accessToken)
        filterChain.doFilter(request, response)
    }

    fun isExcludedPath(path: String): Boolean {
        val excludedPaths = listOf("/h2-console", "/h2-console/**", "/health", "/users/auth", "/auth/refresh")
        return excludedPaths.any { path.startsWith(it) }
    }

    fun checkAccessToken(request: HttpServletRequest): String {
        return try {
            jwtTokenManager.getAccessToken(request).takeIf(jwtTokenManager::isTokenValid)
                ?: run {
                    logger.warn("The access token has expired.")
                    throw AuthenticationException("Access token이 만료되었습니다.")
                }
        } catch (ex: AuthenticationException) {
            request.setAttribute("customAuthErrorMessage", ex.message)
            throw ex
        }
    }

    fun authenticateUser(accessToken: String) {
        val userId = UUID.fromString(jwtTokenManager.parseClaims(accessToken))
        val user = userRepository.findById(userId)
            ?: run {
                logger.warn("Request from an unknown user (userId: $userId).")
                throw AuthenticationException("인증 오류입니다.")
            }
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