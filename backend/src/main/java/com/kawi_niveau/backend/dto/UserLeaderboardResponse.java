package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLeaderboardResponse {
    private UserPositionResponse userPosition;
    private List<LeaderboardEntryResponse> topLeaderboard;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPositionResponse {
        private Integer rank;
        private String name;
        private Integer totalPoints;
        private Integer level;
        private String levelName;
        private Integer badgesCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntryResponse {
        private Integer rank;
        private String name;
        private Integer totalPoints;
        private Integer level;
        private String levelName;
        private Integer badgesCount;
        private Boolean isCurrentUser;
    }
}