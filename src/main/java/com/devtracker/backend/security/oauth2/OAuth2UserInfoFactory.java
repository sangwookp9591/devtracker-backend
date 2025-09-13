package com.devtracker.backend.security.oauth2;

import com.devtracker.backend.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 객체 생성 팩토리
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("github".equalsIgnoreCase(registrationId)) {
            return new GitHubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Sorry! Login with " + registrationId + " is not supported yet."
            );
        }
    }
}
