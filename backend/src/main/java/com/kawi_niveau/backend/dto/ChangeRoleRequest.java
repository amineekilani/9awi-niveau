package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class ChangeRoleRequest {
    private String role; // "ETUDIANT" ou "FORMATEUR"
}
