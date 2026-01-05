package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.ExerciceElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExerciceElementResponse {
    private Long id;
    private String contenu;
    private ExerciceElement.TypeElement typeElement;
    private Integer positionOrdre;
    private String reponseCorrecte;
    private List<String> options;
    private Long createdAt;
}