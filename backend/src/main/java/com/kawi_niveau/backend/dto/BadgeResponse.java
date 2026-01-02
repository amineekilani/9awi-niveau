package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String criteriaType;
    private Integer criteriaValue;
    private Boolean isActive;
    private Long createdAt;
    private Long updatedAt;
    private long usersCount;
}