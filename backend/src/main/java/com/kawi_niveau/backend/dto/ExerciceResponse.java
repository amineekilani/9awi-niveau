package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.Exercice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExerciceResponse {
    private Long id;
    private String titre;
    private String description;
    private Exercice.TypeExercice typeExercice;
    private Long moduleId;
    private List<ExerciceElementResponse> elements;
    private Long createdAt;
    private Long updatedAt;
}