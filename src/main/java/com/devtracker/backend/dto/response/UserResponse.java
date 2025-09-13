package com.devtracker.backend.dto.response;


import com.devtracker.backend.entity.user.enums.DeveloperType;
import com.devtracker.backend.entity.user.enums.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "developer@example.com")
    private String email;

    @Schema(description = "닉네임", example = "개발자님")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://avatar.githubusercontent.com/u/12345")
    private String profileImage;

    @Schema(description = "개발자 타입", example = "FULLSTACK")
    private DeveloperType developerType;

    @Schema(description = "구독 플랜", example = "FREE")
    private SubscriptionPlan subscriptionPlan;

    @Schema(description = "시간대", example = "Asia/Seoul")
    private String timezone;

    @Schema(description = "시급", example = "50000")
    private BigDecimal hourlyRate;

    @Schema(description = "선호 통화", example = "KRW")
    private String preferredCurrency;

    @Schema(description = "GitHub 사용자명", example = "developer123")
    private String githubUsername;

    @Schema(description = "GitLab 사용자명", example = "developer123")
    private String gitlabUsername;

    @Schema(description = "인증 제공자", example = "github")
    private String provider;

    @Schema(description = "이메일 검증 여부", example = "true")
    private Boolean emailVerified;

    @Schema(description = "가입 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;
}