package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String provider;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String profileImage;
    private String role;
}
