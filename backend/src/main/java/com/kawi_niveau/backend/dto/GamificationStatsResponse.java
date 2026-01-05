package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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