package com.devtracker.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devtracker.backend.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 시 처리하는 엔트리 포인트
 * - 401 Unauthorized 응답을 JSON 형태로 반환
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<String> errorResponse = ApiResponse.failure(
                "인증이 필요합니다. 로그인 후 다시 시도해주세요.",
                "UNAUTHORIZED"
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
