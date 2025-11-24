package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank(message = "La question est obligatoire")
    private String question;

    @NotEmpty(message = "Les options sont obligatoires")
    private List<String> options;

    @NotBlank(message = "La réponse correcte est obligatoire")
    private String correctAnswer;

    private Integer ordre;
}
