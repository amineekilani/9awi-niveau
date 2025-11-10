package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String username;
    private String email;
    private String currentPassword;
    private String newPassword;
}
