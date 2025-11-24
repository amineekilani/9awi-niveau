package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private Integer ordre;
    private Long createdAt;
}
