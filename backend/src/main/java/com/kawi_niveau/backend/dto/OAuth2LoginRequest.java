package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class OAuth2LoginRequest {
    private String token; // Google ID token
}
