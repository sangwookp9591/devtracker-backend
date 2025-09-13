package com.devtracker.backend.security.oauth2;

import java.util.Map;

/**
 * GitHub OAuth2 사용자 정보 구현체
 * GitHub API 응답 형식에 맞춰 사용자 정보 추출
 */
public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        return name != null ? name : (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }

    @Override
    public String getLogin() {
        return (String) attributes.get("login");
    }
}
