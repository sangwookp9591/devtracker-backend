package com.devtracker.backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;


/**
 * JWT 토큰 생성, 파싱, 검증을 담당하는 컴포넌트
 *   - Header: 토큰 타입과 알고리즘 정보
 *   - Payload: 사용자 정보 (Claims)
 *   - Signature: 토큰의 무결성을 보장하는 서명
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration}") long jwtExpiration, @Value("${app.jwt.refresh-expiration}") long refreshExpiration){

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Access Token 생성
     * @param authentication 인증 정보
     * @return JWT 토큰 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        return Jwts.builder()
                .subject(userPrincipal.getId().toString()) // 토큰의 주체 (사용자 ID)
                .issuedAt(new Date()) // 토큰 발행 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .claim("email", userPrincipal.getEmail()) // 추가 정보
                .claim("nickname", userPrincipal.getNickname())
                .signWith(secretKey) // 비밀키로 서명
                .compact();
    }
    

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
