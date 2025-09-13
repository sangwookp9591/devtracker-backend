package com.devtracker.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devtracker.backend.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인가 실패 시 처리하는 핸들러
 * - 403 Forbidden 응답을 JSON 형태로 반환
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<String> errorResponse = ApiResponse.failure(
                "접근 권한이 없습니다.",
                "ACCESS_DENIED"
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
