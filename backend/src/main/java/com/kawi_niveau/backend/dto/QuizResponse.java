package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String titre;
    private String description;
    private Long moduleId;
    private List<QuestionResponse> questions;
    private Long createdAt;
    private Long updatedAt;
}
