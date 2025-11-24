package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {
    @NotNull(message = "L'ID du cours est obligatoire")
    private Long coursId;
}
