package com.devtracker.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 * - @EnableJpaAuditing: @CreatedDate, @LastModifiedDate 자동 처리
 * - @EnableJpaRepositories: JPA 레포지토리 자동 스캔 설정
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.devtracker.backend.repository")
public class JpaConfig {
}
