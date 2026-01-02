package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class BadgeRequest {
    private String name;
    private String description;
    private String iconUrl;
    private String criteriaType;
    private Integer criteriaValue;
    private boolean isActive = true;
}