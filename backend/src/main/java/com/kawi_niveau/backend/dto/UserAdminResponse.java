package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAdminResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean emailVerified;
    private boolean archived;
    private Long createdAt;
    private String phoneNumber;
    private String dateOfBirth;
    private Integer failedLoginAttempts;
    private Long accountLockedUntil;
}