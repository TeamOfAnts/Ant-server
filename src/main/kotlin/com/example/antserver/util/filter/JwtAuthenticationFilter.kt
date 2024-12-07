package com.example.antserver.util.filter

import domain.user.*
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthenticationFilter(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val refreshToken = authService.extractRefreshToken(request)?.takeIf(authService::isTokenValid)

        if (refreshToken == null) {
            checkAccessTokenAndAuthenticate(request, response, filterChain)
            return
        }

        checkRefreshTokenAndReIssueAccessToken(response, refreshToken)
    }

    fun checkAccessTokenAndAuthenticate(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = authService.extractAccessToken(request)

        // Access Token이 유효한 경우 사용자 인증
        if (authService.isTokenValid(accessToken)) {
            authService.parseClaims(accessToken)
                ?.let { UUID.fromString(it) }
                ?.let { userId ->
                    userRepository.findById(userId)?.let { user ->
                        if (user.role == UserRoleType.GUEST) {
                            user.updateRole(UserRoleType.MEMBER) // 역할 변경
                            userRepository.save(user) // 변경 내용 저장
                        }
                    }
                }
        } else {
            // Access Token이 유효하지 않은 경우
            val refreshToken = authService.extractRefreshToken(request)
                ?.takeIf(authService::isTokenValid)

            // Refresh Token이 유효한 경우 Access Token 재발급
            if (refreshToken != null) {
                val newAccessToken = authService.generateAccessToken(userId)
                val header = authService.setAccessTokenToHeader(newAccessToken)
            } else { // Refresh Token이 유효하지 않은 경우 Access Token과 Refresh Token 모두 재발급
                authService.parseClaims(accessToken)
                    ?.let { UUID.fromString(it) }
                    ?.let { userId ->
                        userRepository.findByUserId(userId)?.let { user ->
                            val newAccessToken = authService.generateAccessToken(userId)
                            val newRefreshToken = authService.generateRefreshToken()

                            authService.setTokensToResponse(response, newAccessToken, newRefreshToken)
                            user.updateRefreshToken(newRefreshToken)
                            userRepository.save(user) // 새로운 Refresh Token 저장
                        }
                    } ?: run {
                    return
                }
            }
        }
        // 필터 체인 호출
        filterChain.doFilter(request, response)
        }
    }

    fun checkRefreshTokenAndReIssueAccessToken(response: HttpServletResponse, refreshToken: String) {
        val storedRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
            ?: throw IllegalArgumentException("Refresh Token이 존재하지 않습니다.")

        // Access Token 재발급
        val newAccessToken = authService.generateAccessToken(userId = storedRefreshToken.userId)

        // 새로운 Access Token을 세팅
        authService.setAccessTokenToHeader(newAccessToken)
    }
}