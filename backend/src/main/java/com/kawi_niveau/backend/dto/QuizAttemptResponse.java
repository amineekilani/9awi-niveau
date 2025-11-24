package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizAttemptResponse {
    private Long id;
    private Double score;
    private Long datePassed;
    private Integer reponsesCorrectes;
    private Integer nombreQuestions;
}
