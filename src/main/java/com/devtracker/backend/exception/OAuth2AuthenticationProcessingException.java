package com.devtracker.backend.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * OAuth2 인증 처리 중 발생하는 예외
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    
    public OAuth2AuthenticationProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}