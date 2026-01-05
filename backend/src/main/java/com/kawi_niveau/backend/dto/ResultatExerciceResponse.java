package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultatExerciceResponse {
    private Long id;
    private Long userId;
    private Long exerciceId;
    private String exerciceTitre;
    private Double score;
    private Long datePassed;
    private Integer nombreElements;
    private Integer reponsesCorrectes;
    private Integer tempsPasse;
    private List<ElementResultat> details;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ElementResultat {
        private Long elementId;
        private String contenu;
        private String reponseUtilisateur;
        private String reponseCorrecte;
        private Boolean correct;
    }
}