package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeconCompletionRequest {
    @NotNull(message = "L'ID de la leçon est obligatoire")
    private Long leconId;
}
