package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String provider;
    private boolean emailVerified;
}
