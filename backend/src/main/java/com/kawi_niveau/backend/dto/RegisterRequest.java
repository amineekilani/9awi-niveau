package com.kawi_niveau.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String phoneNumber;
}
