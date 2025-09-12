package com.devtracker.backend.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {
    FREE("무료", 3, false),
    BASIC("베이직", 10, true),
    PRO("프로", Integer.MAX_VALUE, true);
    
    private final String displayName;
    private final int maxProjects;
    private final boolean gitIntegrationEnabled;
}
