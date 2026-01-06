package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class LevelNotificationResponse {
    private Long id;
    private Integer oldLevel;
    private Integer newLevel;
    private String levelName;
    private Integer totalXP;
    private Integer xpGained;
    private Boolean isRead;
    private Boolean isNew;
    private Long createdAt;
    private String timeAgo;
}