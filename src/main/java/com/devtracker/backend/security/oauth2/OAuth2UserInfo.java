package com.devtracker.backend.security.oauth2;


import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보를 추상화한 인터페이스
 * - 각 OAuth2 제공자(GitHub, Google 등)마다 다른 응답 형식을 통일
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    public abstract String getLogin(); // GitHub 전용
}