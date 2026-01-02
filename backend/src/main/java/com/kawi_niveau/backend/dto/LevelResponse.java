package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class LevelResponse {
    private Long id;
    private Integer level;
    private Integer xpRequired;
    private String name;
    private String description;
    private Long createdAt;
}