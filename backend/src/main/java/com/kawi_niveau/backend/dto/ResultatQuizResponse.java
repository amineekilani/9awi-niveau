package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultatQuizResponse {
    private Long id;
    private Long userId;
    private Long quizId;
    private String quizTitre;
    private Double score;
    private Long datePassed;
    private Integer nombreQuestions;
    private Integer reponsesCorrectes;
    private Integer tempsPasse;
    private List<QuestionResultat> details;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResultat {
        private Long questionId;
        private String question;
        private String reponseUtilisateur;
        private String reponseCorrecte;
        private Boolean correct;
    }
}
