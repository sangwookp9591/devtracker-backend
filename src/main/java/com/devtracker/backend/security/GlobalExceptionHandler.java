package com.devtracker.backend.security;

import com.devtracker.backend.dto.response.ApiResponse;
import com.devtracker.backend.exception.BadRequestException;
import com.devtracker.backend.exception.ResourceNotFoundException;
import com.devtracker.backend.exception.OAuth2AuthenticationProcessingException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * - Spring Boot 초보자 설명:
 *   @RestControllerAdvice: 모든 컨트롤러에서 발생하는 예외를 중앙에서 처리
 *   @ExceptionHandler: 특정 예외 타입에 대한 처리 메서드 지정
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 유효성 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("입력 데이터가 올바르지 않습니다: " + errorMessage, "VALIDATION_ERROR"));
    }

    /**
     * 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<String>> handleBindException(BindException e) {
        log.warn("Bind error: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("입력 데이터가 올바르지 않습니다: " + errorMessage, "BIND_ERROR"));
    }

    /**
     * 잘못된 요청 예외 처리
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequestException(BadRequestException e) {
        log.warn("Bad request: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(e.getMessage(), "BAD_REQUEST"));
    }

    /**
     * 리소스 없음 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(e.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    /**
     * 인증 실패 예외 처리
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(Exception e) {
        log.warn("Authentication failed: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("이메일 또는 비밀번호가 올바르지 않습니다.", "AUTHENTICATION_FAILED"));
    }

    /**
     * OAuth2 인증 처리 예외
     */
    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleOAuth2AuthenticationException(OAuth2AuthenticationProcessingException e) {
        log.error("OAuth2 authentication error: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(e.getMessage(), "OAUTH2_AUTHENTICATION_ERROR"));
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception: ", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
    }

    /**
     * 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("예상치 못한 오류가 발생했습니다.", "UNEXPECTED_ERROR"));
    }
}