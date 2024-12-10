//package com.example.antserver.util.filter//package com.example.antserver.util.filter
//
//import jakarta.servlet.FilterChain
//import jakarta.servlet.http.HttpServletRequest
//import jakarta.servlet.http.HttpServletResponse
//import org.springframework.stereotype.Component
//import org.springframework.web.filter.OncePerRequestFilter
//import java.util.*
//
//import com.example.antserver.application.AuthService
//import com.example.antserver.domain.auth.RefreshTokenRepository
//import com.example.antserver.domain.user.UserRepository
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.context.SecurityContextHolder
//
//@Component
//class JwtAuthenticationFilter(
//    private val authService: AuthService,
//    private val userRepository: UserRepository,
//    private val refreshTokenRepository: RefreshTokenRepository
//): OncePerRequestFilter() {
//    override fun doFilterInternal(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        filterChain: FilterChain,
//    ) {
//        val refreshToken = authService.getRefreshToken(request)?.takeIf(authService::isTokenValid)
//
//        if (refreshToken == null) {
//            if (checkAccessTokenAndAuthenticate(request, response, filterChain)) {
//                reissueTokens(request, response)
//            }
//            return
//        }
//        checkRefreshTokenAndReissueAccessToken(response, refreshToken)
//        filterChain.doFilter(request, response)
//    }
//
//    fun checkAccessTokenAndAuthenticate(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        filterChain: FilterChain
//    ): Boolean {
//        val accessToken = authService.getAccessToken(request)?.takeIf(authService::isTokenValid)
//            ?: return false
//
//        authService.parseClaims(accessToken)
//            ?.let { UUID.fromString(it) }
//            ?.let { userId ->
//                val user = userRepository.findById(userId)
//                val userDetails = org.springframework.security.core.userdetails.User.builder()
//                    .username(user?.email)
////                            .password("")
////                            .roles(user.role.name)
//                    .build()
//
//                val authentication = UsernamePasswordAuthenticationToken(
//                    userDetails,
//                    null,
//                    userDetails.authorities
//                )
//                SecurityContextHolder.getContext().authentication = authentication
//                }
//
//            return true
//        }
//
//        fun reissueTokens(
//            request: HttpServletRequest,
//            response: HttpServletResponse) {
//
////            val userId = authService.getUserIdFromRequest(request)
////            val refreshToken = authService.getRefreshToken(request)?.takeIf(authService::isTokenValid)
////
////            // Refresh Token이 유효한 경우 Access Token 재발급
////            if (refreshToken != null) {
////                val newAccessToken = authService.generateAccessToken(userId)
////            } else { // Refresh Token이 유효하지 않은 경우 Access Token과 Refresh Token 모두 재발급
////                val userId = authService.getUserIdFromRequest(request)
////                val newAccessToken = authService.generateAccessToken(userId)
////                val newRefreshToken = authService.generateRefreshToken()
////
////                refreshToken = refreshTokenRepository.findByUserId(user.id)
////                refreshToken.updateToken(newRefreshToken)
////                return
//            }
//        }
//
//
//    fun checkRefreshTokenAndReissueAccessToken(response: HttpServletResponse, refreshToken: String) {
////        val storedRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
////            ?: throw IllegalArgumentException("Refresh Token이 존재하지 않습니다.")
////
////        // Access Token 재발급
////        val newAccessToken = authService.generateAccessToken(storedRefreshToken.userId)
////        return newAccessToken
//}