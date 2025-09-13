package com.devtracker.backend.controller.dto.request;


import com.devtracker.backend.entity.user.enums.DeveloperType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 회원가입 요청 DTO
 * - Spring Boot 초보자 설명:
 *   DTO(Data Transfer Object): 계층간 데이터 전송을 위한 객체
 *   @Valid와 함께 사용하여 요청 데이터의 유효성을 자동 검증
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일 주소", example = "developer@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]*$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    @Schema(description = "비밀번호 확인", example = "password123!")
    private String confirmPassword;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
    @Schema(description = "닉네임", example = "개발자님")
    private String nickname;

    @NotNull(message = "개발자 타입은 필수입니다.")
    @Schema(description = "개발자 타입", example = "FULLSTACK")
    private DeveloperType developerType;

    @DecimalMin(value = "0", message = "시급은 0 이상이어야 합니다.")
    @Schema(description = "시급 (원)", example = "50000")
    private BigDecimal hourlyRate;

    @Schema(description = "선호 통화", example = "KRW")
    private String preferredCurrency = "KRW";

    @Schema(description = "GitHub 사용자명", example = "developer123")
    private String githubUsername;

    // 비밀번호 확인 검증을 위한 커스텀 메서드
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
