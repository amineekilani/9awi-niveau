package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeResponse {
    private Long id;
    private String name;
    private String description;
    private String challengeType;
    private Integer targetValue;
    private Integer xpReward;
    private Long startDate;
    private Long endDate;
    private Boolean isActive;
    private Long createdAt;
    private Long updatedAt;
    private long participantsCount;
    private long completedCount;
}