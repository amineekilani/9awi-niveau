package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChallengeResponse {
    private Long id;
    private String name;
    private String description;
    private String challengeType;
    private Integer targetValue;
    private Integer currentProgress;
    private Double progressPercent;
    private Integer xpReward;
    private Boolean isCompleted;
    private Long completedAt;
    private Long joinedAt;
    private Long endDate;
    private String timeRemaining;
    private Boolean isNew;
    private Boolean isActive;
}