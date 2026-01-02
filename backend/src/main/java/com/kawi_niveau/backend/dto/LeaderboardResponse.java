package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardResponse {
    private List<LeaderboardEntry> entries;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LeaderboardEntry {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private Integer totalXP;
        private Integer currentLevel;
        private String levelName;
        private Long badgesCount;
        private Integer rank;
    }
}