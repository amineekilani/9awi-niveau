package com.kawi_niveau.backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExerciceSubmissionRequest {
    private Map<Long, String> reponses; // elementId -> reponse utilisateur
    private Integer tempsPasse; // en secondes
}