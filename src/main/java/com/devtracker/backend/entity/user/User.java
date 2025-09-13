package com.devtracker.backend.entity.user;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.devtracker.backend.entity.BaseTimeEntity;
import com.devtracker.backend.entity.user.enums.DeveloperType;
import com.devtracker.backend.entity.user.enums.SubscriptionPlan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 * Entoty -> 테이블과 매핑되는 JPA 엔티티
 * Table : 테이블명과 인덱스지정
 * @Id: 기본키
 * @GenratedValue :기본키 생성 전략 
 */
@Entity 
@Table(name ="users",
indexes = {
    @Index(name ="idx_user_email", columnList = "email"),
    @Index(name = "idx_user_github", columnList = "githubUsername")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseTimeEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password; // OAuth2 로그인시에는 null일 수 있음

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING) // ENUM을 문자열로 저장
    @Column(name = "developer_type", nullable = false, length = 20)
    private DeveloperType developerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", length = 10)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Seoul";

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "preferred_currency", length = 3)
    private String preferredCurrency = "KRW";

    @Column(name = "github_username", length = 100)
    private String githubUsername;

    @Column(name = "gitlab_username", length = 100)
    private String gitlabUsername;

    // OAuth2 관련 필드들
    @Column(name = "provider")
    private String provider; // "github", "local", "kakao" 등

    @Column(name = "provider_id")
    private String providerId; // GitHub ID 등

    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    

    @Builder
    public User(String email, String password, String nickname, String profileImage,
                DeveloperType developerType, SubscriptionPlan subscriptionPlan,
                String timezone, BigDecimal hourlyRate, String preferredCurrency,
                String githubUsername, String gitlabUsername,
                String provider, String providerId, Boolean emailVerified) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.developerType = developerType;
        this.subscriptionPlan = subscriptionPlan != null ? subscriptionPlan : SubscriptionPlan.FREE;
        this.timezone = timezone != null ? timezone : "Asia/Seoul";
        this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
        this.preferredCurrency = preferredCurrency != null ? preferredCurrency : "KRW";
        this.githubUsername = githubUsername;
        this.gitlabUsername = gitlabUsername;
        this.provider = provider;
        this.providerId = providerId;
        this.emailVerified = emailVerified != null ? emailVerified : false;
    }


        // 비즈니스 로직 메서드들
    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void updateDeveloperInfo(DeveloperType developerType, BigDecimal hourlyRate) {
        if (developerType != null) {
            this.developerType = developerType;
        }
        if (hourlyRate != null) {
            this.hourlyRate = hourlyRate;
        }
    }

    public void updateGitHubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

}
