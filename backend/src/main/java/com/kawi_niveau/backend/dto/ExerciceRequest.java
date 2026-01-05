package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.Exercice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExerciceRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotNull(message = "Le type d'exercice est obligatoire")
    private Exercice.TypeExercice typeExercice;

    private List<ExerciceElementRequest> elements;
}