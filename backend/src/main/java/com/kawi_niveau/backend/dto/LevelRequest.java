package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class LevelRequest {
    private Integer level;
    private Integer xpRequired;
    private String name;
    private String description;
}