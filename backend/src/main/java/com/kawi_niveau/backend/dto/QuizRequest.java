package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class QuizRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    private List<QuestionRequest> questions;
}
