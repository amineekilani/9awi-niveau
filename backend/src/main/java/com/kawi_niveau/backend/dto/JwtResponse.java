package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String email;

    public JwtResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
}