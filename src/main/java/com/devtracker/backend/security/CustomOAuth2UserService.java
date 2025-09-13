package com.devtracker.backend.security;

import com.devtracker.backend.entity.user.User;
import com.devtracker.backend.entity.user.enums.*;
import com.devtracker.backend.exception.OAuth2AuthenticationProcessingException;
import com.devtracker.backend.repository.UserRepository;
import com.devtracker.backend.security.oauth2.OAuth2UserInfo;
import com.devtracker.backend.security.oauth2.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * OAuth2 로그인 처리 서비스
 * - GitHub에서 받은 사용자 정보를 처리하여 데이터베이스에 저장하거나 업데이트
 * - 기존 사용자인 경우 정보 업데이트, 신규 사용자인 경우 회원가입 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 인증 후 사용자 정보 처리
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser() 호출하여 OAuth2User 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * OAuth2 사용자 정보 처리 메인 로직
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // OAuth2 제공자별 사용자 정보 추출 (현재는 GitHub만 지원)
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        // 필수 정보 검증
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        // 기존 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            // 기존 사용자 - 정보 업데이트
            user = userOptional.get();
            if (!user.getProvider().equals(oAuth2UserRequest.getClientRegistration().getRegistrationId())) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with " + user.getProvider() +
                        " account. Please use your " + user.getProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // 신규 사용자 - 회원가입 처리
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    /**
     * 신규 사용자 등록
     */
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        log.info("Registering new user with email: {}", oAuth2UserInfo.getEmail());

        User user = User.builder()
                .nickname(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .provider(oAuth2UserRequest.getClientRegistration().getRegistrationId())
                .providerId(oAuth2UserInfo.getId())
                .emailVerified(true) // OAuth2 로그인은 이메일이 검증된 것으로 간주
                .developerType(DeveloperType.OTHER) // 기본값, 나중에 사용자가 변경 가능
                .githubUsername(oAuth2UserInfo.getLogin()) // GitHub 전용
                .build();

        return userRepository.save(user);
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        log.info("Updating existing user: {}", existingUser.getEmail());

        // 프로필 이미지나 이름이 변경되었다면 업데이트
        boolean needUpdate = false;

        if (!existingUser.getNickname().equals(oAuth2UserInfo.getName())) {
            existingUser.updateProfile(oAuth2UserInfo.getName(), existingUser.getProfileImage());
            needUpdate = true;
        }

        if (oAuth2UserInfo.getImageUrl() != null && 
            !oAuth2UserInfo.getImageUrl().equals(existingUser.getProfileImage())) {
            existingUser.updateProfile(existingUser.getNickname(), oAuth2UserInfo.getImageUrl());
            needUpdate = true;
        }

        // GitHub 사용자명 업데이트
        if (oAuth2UserInfo.getLogin() != null && 
            !oAuth2UserInfo.getLogin().equals(existingUser.getGithubUsername())) {
            existingUser.updateGitHubUsername(oAuth2UserInfo.getLogin());
            needUpdate = true;
        }

        return needUpdate ? userRepository.save(existingUser) : existingUser;
    }
}