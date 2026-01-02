package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;
    private boolean emailVerified;
}