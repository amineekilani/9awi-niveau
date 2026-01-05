package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.ExerciceElement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExerciceElementRequest {
    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    @NotNull(message = "Le type d'élément est obligatoire")
    private ExerciceElement.TypeElement typeElement;

    @NotNull(message = "La position est obligatoire")
    private Integer positionOrdre;

    private String reponseCorrecte;

    private List<String> options;
}