package util.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import util.filter.CustomAccessDeniedHandler
import util.filter.CustomAuthenticationEntryPoint
import util.filter.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler
) {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
            http.csrf { it.disable() }
                .httpBasic { it.disable() }
                .formLogin { it.disable() }
                .headers { it.frameOptions { frameOptions -> frameOptions.sameOrigin() } }
                .cors { } // CORS 에러 방지용
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Stateless 세션 설정
                .authorizeHttpRequests { auth ->
                    auth.requestMatchers(HttpMethod.OPTIONS).permitAll() // CORS Preflight 방지
                    auth.requestMatchers("/", "/h2-console/**", "/member/login/**").permitAll() // 공용 경로
                    auth.anyRequest().authenticated() // 그 외는 인증 필요
                }
                // JWT 토큰 예외 처리
                .exceptionHandling { exceptions ->
                    exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                    exceptions.accessDeniedHandler(customAccessDeniedHandler)
                }
                .addFilterBefore(JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter::class.java)

            return http.build()
        }
}