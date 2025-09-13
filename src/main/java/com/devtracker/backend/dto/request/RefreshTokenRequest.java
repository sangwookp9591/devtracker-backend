package com.devtracker.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 토큰 갱신 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "토큰 갱신 요청")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token은 필수입니다.")
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
