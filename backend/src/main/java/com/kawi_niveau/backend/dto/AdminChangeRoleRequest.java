package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class AdminChangeRoleRequest {
    private Long userId;
    private String role; // "ETUDIANT", "FORMATEUR", ou "ADMIN"
}