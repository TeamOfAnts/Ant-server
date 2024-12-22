package com.example.antserver.util.config

import com.example.antserver.util.exception.CustomAccessDeniedHandler
import com.example.antserver.util.exception.CustomAuthenticationEntryPoint
import com.example.antserver.util.exception.GlobalExceptionHandler
import com.example.antserver.util.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    fun corsConfigurationSource(): CorsConfigurationSource {
        return CorsConfigurationSource { request ->
            val config = CorsConfiguration()
            config.allowedHeaders = Collections.singletonList("*")
            config.allowedMethods = Collections.singletonList("*")
            config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:8888"))
            config.allowCredentials = true
            config
        }
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity, globalExceptionHandler: GlobalExceptionHandler): SecurityFilterChain {
        http.csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .headers { it.frameOptions { frameOptions -> frameOptions.sameOrigin() } }
            .cors { corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()) } // CORS 에러 방지용 TODO. 도메인 추가
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Stateless 세션 설정
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS).permitAll() // CORS Preflight 방지
                auth.requestMatchers("/health", "/users/auth", "/auth/refresh").permitAll()
                auth.anyRequest().authenticated() // 그 외는 인증 필요
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                exceptions.accessDeniedHandler(customAccessDeniedHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
