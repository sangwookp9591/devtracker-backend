package com.devtracker.backend.repository;

import com.devtracker.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 데이터 접근 레포지토리
 * - Spring Boot 초보자 설명:
 *   JpaRepository를 상속받으면 기본적인 CRUD 메서드들이 자동 생성
 *   - save(), findById(), findAll(), delete() 등
 *   추가로 필요한 쿼리 메서드들을 정의할 수 있음
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * Spring Data JPA의 메서드 네이밍 규칙에 따라 자동으로 쿼리 생성
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * GitHub 사용자명으로 조회
     */
    Optional<User> findByGithubUsername(String githubUsername);

    /**
     * GitHub 사용자명 존재 여부 확인
     */
    boolean existsByGithubUsername(String githubUsername);

    /**
     * 제공자와 제공자 ID로 사용자 조회 (OAuth2용)
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 활성 사용자만 조회 (복잡한 쿼리는 @Query 사용)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.emailVerified = true")
    Optional<User> findByEmailAndVerified(@Param("email") String email);

    /**
     * 특정 개발자 타입의 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.developerType = :developerType")
    Long countByDeveloperType(@Param("developerType") com.devtracker.backend.entity.user.enums.DeveloperType developerType);
}
