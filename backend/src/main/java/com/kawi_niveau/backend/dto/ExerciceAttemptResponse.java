package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExerciceAttemptResponse {
    private Long id;
    private Double score;
    private Long datePassed;
    private Integer reponsesCorrectes;
    private Integer nombreElements;
}