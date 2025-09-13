package com.devtracker.backend.controller;


import com.devtracker.backend.dto.request.LoginRequest;
import com.devtracker.backend.dto.request.SignUpRequest;
import com.devtracker.backend.dto.request.RefreshTokenRequest;
import com.devtracker.backend.dto.response.ApiResponse;
import com.devtracker.backend.dto.response.AuthResponse;
import com.devtracker.backend.dto.response.UserResponse;
import com.devtracker.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 * - Spring Boot 초보자 설명:
 *   @RestController: 이 클래스의 모든 메서드가 JSON 응답을 반환하는 REST API
 *   @RequestMapping: 이 컨트롤러의 기본 URL 경로
 *   @PostMapping: HTTP POST 요청을 처리하는 메서드
 *   @Valid: 요청 데이터의 유효성 검증
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 일반 회원가입 (이메일/비밀번호)
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 신규 사용자 등록")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Sign up request for email: {}", signUpRequest.getEmail());
        
        try {
            UserResponse userResponse = authService.signUp(signUpRequest);
            
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("회원가입이 완료되었습니다.")
                    .data(userResponse)
                    .build());
                    
        } catch (Exception e) {
            log.error("Sign up failed for email: {}", signUpRequest.getEmail(), e);
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("회원가입에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 로그인 (이메일/비밀번호)
     */
    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인")
    public ResponseEntity<ApiResponse<AuthResponse>> signIn(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Sign in request for email: {}", loginRequest.getEmail());
        
        try {
            AuthResponse authResponse = authService.signIn(loginRequest);
            
            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("로그인이 완료되었습니다.")
                    .data(authResponse)
                    .build());
                    
        } catch (Exception e) {
            log.error("Sign in failed for email: {}", loginRequest.getEmail(), e);
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message("로그인에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 토큰 갱신 (Refresh Token으로 새로운 Access Token 발급)
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token 발급")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Token refresh request");
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);
            
            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("토큰이 갱신되었습니다.")
                    .data(authResponse)
                    .build());
                    
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message("토큰 갱신에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    /**
     * GitHub OAuth2 로그인 시작
     * 실제 로그인은 /oauth2/authorization/github로 리다이렉트되어 처리됨
     */
    @GetMapping("/oauth2/github")
    @Operation(summary = "GitHub OAuth2 로그인", description = "GitHub 계정으로 로그인")
    public ResponseEntity<ApiResponse<String>> githubLogin() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("GitHub 로그인을 시작합니다.")
                .data("/oauth2/authorization/github")
                .build());
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        try {
            UserResponse userResponse = authService.getCurrentUser();
            
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("사용자 정보를 조회했습니다.")
                    .data(userResponse)
                    .build());
                    
        } catch (Exception e) {
            log.error("Get current user failed", e);
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("사용자 정보 조회에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 로그아웃 (실제로는 클라이언트에서 토큰 삭제)
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 처리")
    public ResponseEntity<ApiResponse<String>> logout() {
        // JWT는 Stateless이므로 서버에서 특별한 처리 불필요
        // 클라이언트에서 토큰을 삭제하면 됨
        // 향후 Refresh Token을 Redis에 저장한다면 여기서 삭제 처리
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("로그아웃되었습니다.")
                .data("SUCCESS")
                .build());
    }
}
