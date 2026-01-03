package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGamificationStatsResponse {
    private Integer totalPoints;
    private Integer currentLevel;
    private String levelName;
    private String levelDescription;
    private Integer pointsToNextLevel;
    private Integer nextLevelPoints;
    private Double progressPercent;
    private Integer badgesCount;
    private Integer completedChallenges;
    private Integer leaderboardPosition;
    private List<RecentActivityResponse> recentActivities;
    private List<UserBadgeResponse> recentBadges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityResponse {
        private String type;
        private String description;
        private Integer points;
        private String timeAgo;
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBadgeResponse {
        private Long id;
        private String name;
        private String description;
        private String criteriaType; // Added field
        private String iconUrl;
        private Long earnedAt;
        private Boolean isNew;
    }
}