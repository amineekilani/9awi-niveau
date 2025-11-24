package com.kawi_niveau.backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmissionRequest {
    private Map<Long, String> reponses; // questionId -> reponse choisie
    private Integer tempsPasse; // en secondes
}
