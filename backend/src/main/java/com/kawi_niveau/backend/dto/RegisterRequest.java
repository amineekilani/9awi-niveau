package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, message = "Minimum 3 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;
}