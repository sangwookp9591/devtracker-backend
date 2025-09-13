package com.devtracker.backend.config;

import com.devtracker.backend.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 * - Spring Boot 초보자 설명:
 *   Spring Security는 인증(Authentication)과 인가(Authorization)를 담당
 *   - 인증: 사용자가 누구인지 확인
 *   - 인가: 인증된 사용자가 특정 리소스에 접근할 권한이 있는지 확인
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 사용 가능
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    /**
     * 비밀번호 인코더 빈 등록
     * BCrypt는 현재 가장 안전한 해싱 알고리즘 중 하나
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈 등록
     * 인증을 처리하는 핵심 컴포넌트
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * JWT 인증 필터
     * 요청마다 JWT 토큰을 검증하여 사용자 인증 처리
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /**
     * CORS 설정
     * 프론트엔드(React Native)에서 API 호출 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 개발용, 운영에서는 구체적 도메인 지정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 보안 필터 체인 설정
     * Spring Security의 핵심 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용시 불필요)
                .csrf(csrf -> csrf.disable())
                
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 세션 비활성화 (JWT는 Stateless)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 예외 처리 핸들러 등록
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                    // 인증 없이 접근 가능한 경로들
                    .requestMatchers(
                        "/api/v1/auth/**",           // 인증 관련 API
                        "/oauth2/**",                // OAuth2 관련
                        "/api/v1/public/**",         // 공개 API
                        "/actuator/health",          // Health Check
                        "/v3/api-docs/**",           // Swagger 문서
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    
                    // 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated())
                
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(authorization -> authorization
                        .baseUri("/oauth2/authorization")) // GitHub 로그인 시작점
                    .redirectionEndpoint(redirection -> redirection
                        .baseUri("/oauth2/callback/*")) // GitHub에서 콜백 받는 경로
                    .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)) // 사용자 정보 처리 서비스
                    .successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 핸들러
                    .failureHandler(oAuth2AuthenticationFailureHandler)) // 로그인 실패 핸들러
                
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
