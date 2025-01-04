package com.example.antserver.util.security.jwt

import com.example.antserver.application.auth.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import com.example.antserver.domain.user.UserRepository
import com.example.antserver.util.exception.ApplicationException
import com.example.antserver.util.exception.AuthenticationException
import com.example.antserver.util.response.Status
import com.example.antserver.util.response.writeErrorResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Component
class JwtAuthenticationFilter(
    private val jwtTokenManager: JwtTokenManager,
    private val tokenService: TokenService,
    private val userRepository: UserRepository
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val accessToken = jwtTokenManager.getAccessToken(request)
            tokenService.isTokenValid(accessToken)
            authenticateUser(accessToken)
        } catch (exception: Exception) {
            when (exception) {
                // NOTE: access token 만료 에러의 경우 refresh 해야하기 때문에 에러 응답을 생성한다.
                is AuthenticationException -> {
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, exception.message)
                    return
                }
                // NOTE: access token이 없거나 유효하지 않아도 허용된 요청이라면, 에러가 filter chain 밖으로 전파되지 않도록 catch해서 doFilter한다.
                is NullPointerException, is ApplicationException -> {
                    filterChain.doFilter(request, response)
                    return
                }
                else -> throw exception
            }
        }
        filterChain.doFilter(request, response)
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