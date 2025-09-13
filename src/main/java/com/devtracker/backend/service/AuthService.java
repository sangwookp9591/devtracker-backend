package com.devtracker.backend.service;

import com.devtracker.backend.dto.request.LoginRequest;
import com.devtracker.backend.dto.request.RefreshTokenRequest;
import com.devtracker.backend.dto.request.SignUpRequest;
import com.devtracker.backend.dto.response.AuthResponse;
import com.devtracker.backend.dto.response.UserResponse;
import com.devtracker.backend.entity.user.User;
import com.devtracker.backend.exception.BadRequestException;
import com.devtracker.backend.exception.ResourceNotFoundException;
import com.devtracker.backend.repository.UserRepository;
import com.devtracker.backend.security.JwtTokenProvider;
import com.devtracker.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 - 회원가입, 로그인, 토큰 관리 등의 비즈니스 로직
 * - Spring Boot 초보자 설명:
 *   @Service: 비즈니스 로직을 처리하는 서비스 계층
 *   @Transactional: 데이터베이스 트랜잭션 관리 (오류시 롤백)
 *   의존성 주입: 생성자를 통해 필요한 객체들을 주입받아 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * 일반 회원가입 (이메일/비밀번호)
     */
    @Transactional // 쓰기 작업이므로 별도 트랜잭션 설정
    public UserResponse signUp(SignUpRequest signUpRequest) {
        log.info("Processing sign up for email: {}", signUpRequest.getEmail());

        // 1. 입력 데이터 검증
        validateSignUpRequest(signUpRequest);

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }

        // 3. 비밀번호 확인 검증
        if (!signUpRequest.isPasswordMatching()) {
            throw new BadRequestException("비밀번호가 일치하지 않습니다.");
        }

        // 4. 사용자 엔티티 생성
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword())) // 비밀번호 암호화
                .nickname(signUpRequest.getNickname())
                .developerType(signUpRequest.getDeveloperType())
                .hourlyRate(signUpRequest.getHourlyRate())
                .preferredCurrency(signUpRequest.getPreferredCurrency())
                .githubUsername(signUpRequest.getGithubUsername())
                .provider("local") // 일반 회원가입
                .emailVerified(false) // 이메일 인증 필요 (향후 구현)
                .build();

        // 5. 데이터베이스에 저장
        User savedUser = userRepository.save(user);
        
        log.info("User successfully registered with ID: {}", savedUser.getId());

        // 6. 응답 DTO로 변환하여 반환
        return convertToUserResponse(savedUser);
    }

    /**
     * 로그인 (이메일/비밀번호)
     */
    @Transactional
    public AuthResponse signIn(LoginRequest loginRequest) {
        log.info("Processing sign in for email: {}", loginRequest.getEmail());

        // 1. 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // 2. 인증 성공시 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. JWT 토큰 생성
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal.getId());

        // 4. 사용자 정보 조회 (최신 정보 반영)
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        log.info("User successfully signed in: {}", user.getEmail());

        // 5. 응답 생성
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // 초 단위로 변환
                .user(convertToUserResponse(user))
                .build();
    }

    /**
     * 토큰 갱신 (Refresh Token으로 새로운 Access Token 발급)
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("Processing token refresh");

        // 1. Refresh Token 검증
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 4. 새로운 토큰들 생성
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        log.info("Tokens refreshed for user: {}", user.getEmail());

        // 5. 응답 생성
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(convertToUserResponse(user))
                .build();
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    public UserResponse getCurrentUser() {
        // SecurityContext에서 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("인증되지 않은 사용자입니다.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 최신 사용자 정보 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        return convertToUserResponse(user);
    }

    /**
     * 회원가입 요청 유효성 검증
     */
    private void validateSignUpRequest(SignUpRequest signUpRequest) {
        // 추가적인 비즈니스 로직 검증
        if (signUpRequest.getHourlyRate() != null && signUpRequest.getHourlyRate().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("시급은 0 이상이어야 합니다.");
        }

        // GitHub 사용자명이 있다면 중복 체크
        if (signUpRequest.getGithubUsername() != null && 
            userRepository.existsByGithubUsername(signUpRequest.getGithubUsername())) {
            throw new BadRequestException("이미 사용 중인 GitHub 사용자명입니다.");
        }
    }

    /**
     * User 엔티티를 UserResponse DTO로 변환
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .developerType(user.getDeveloperType())
                .subscriptionPlan(user.getSubscriptionPlan())
                .timezone(user.getTimezone())
                .hourlyRate(user.getHourlyRate())
                .preferredCurrency(user.getPreferredCurrency())
                .githubUsername(user.getGithubUsername())
                .gitlabUsername(user.getGitlabUsername())
                .provider(user.getProvider())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

