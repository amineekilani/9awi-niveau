package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String email;
    private String role;
    private String domaineSpecialisation;

    public JwtResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public JwtResponse(String token, String email, String role, String domaineSpecialisation) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.domaineSpecialisation = domaineSpecialisation;
    }
}