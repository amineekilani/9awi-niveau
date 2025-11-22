package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String email;
    private String currentPassword;
    private String newPassword;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
}
