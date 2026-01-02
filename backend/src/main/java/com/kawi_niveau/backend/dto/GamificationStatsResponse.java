package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GamificationStatsResponse {
    private long totalBadges;
    private long activeBadges;
    private long totalChallenges;
    private long activeChallenges;
    private long totalXPAwarded;
    private double averageUserXP;
    private long totalBadgesEarned;
    private long totalChallengesCompleted;
}