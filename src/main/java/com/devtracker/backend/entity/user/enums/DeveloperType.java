package com.devtracker.backend.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeveloperType {
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    FULLSTACK("풀스택"),
    MOBILE("모바일"),
    DESIGNER("디자이너"),
    DEVOPS("데브옵스"),
    OTHER("기타");

    private final String displayName;
}
