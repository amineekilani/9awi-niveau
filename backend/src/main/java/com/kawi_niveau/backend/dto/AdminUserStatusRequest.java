package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class AdminUserStatusRequest {
    private Long userId;
    private boolean archived;
}