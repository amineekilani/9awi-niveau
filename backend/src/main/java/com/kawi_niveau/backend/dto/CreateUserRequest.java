package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String role; // "ETUDIANT", "FORMATEUR", ou "ADMIN"
    private String phoneNumber;
    private String dateOfBirth;
    private boolean emailVerified = true; // Admin peut créer des comptes pré-vérifiés
}