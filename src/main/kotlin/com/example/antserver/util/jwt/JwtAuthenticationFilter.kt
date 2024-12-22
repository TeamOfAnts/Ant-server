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
        try{
            val accessToken = checkAccessToken(request)
            authenticateUser(accessToken)
        }catch (ex: AuthenticationException){
            // NOTE: access token 만료 에러의 경우 refresh를 해야하기 때문에 throw한다.
            if(ex.message == "Access token이 만료되었습니다."){
                throw ex
            }
        }finally {
            filterChain.doFilter(request, response)
        }
    }

    fun checkAccessToken(request: HttpServletRequest): String {
            return jwtTokenManager.getAccessToken(request).takeIf(jwtTokenManager::isTokenValid)
                ?: run {
                    logger.warn("The access token has expired.")
                    throw AuthenticationException("Access token이 만료되었습니다.")
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