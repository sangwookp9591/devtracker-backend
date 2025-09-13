package com.devtracker.backend.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.devtracker.backend.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring Security에서 사용하는 사용자 정보 클래스
 * UserDetails와 OAuth2User 인터페이스를 모두 구현하여
 * 일반 로그인과 OAuth2 로그인을 모두 지원
 */

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {
    
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // OAuth2 사용자 속성

    // User 엔티티로부터 UserPrincipal 생성
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                authorities,
                null
        );
    }

    // OAuth2 사용자 정보와 함께 UserPrincipal 생성
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(user);
        userPrincipal.attributes = attributes;
        return userPrincipal;
    }

    // 간단한 생성자 (JWT 필터에서 사용)
    public static UserPrincipal create(Long userId) {
        return new UserPrincipal(
                userId,
                null,
                null,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                null
        );
    }

    // UserDetails 인터페이스 구현
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User 인터페이스 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}