package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class ChallengeRequest {
    private String name;
    private String description;
    private String challengeType;
    private Integer targetValue;
    private Integer xpReward;
    private Long startDate;
    private Long endDate;
    private boolean isActive = true;
}