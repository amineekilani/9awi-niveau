package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprenantProgressionResponse {
    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private String profileImage;
    private Float progressionGlobale;
    private Integer totalLecons;
    private Integer leconsCompletees;
    private Long enrolledAt;
    private Long lastAccessedAt;
    private List<ModuleProgressionDetail> modulesProgression;
    private List<QuizResultatDetail> quizResultats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModuleProgressionDetail {
        private Long moduleId;
        private String moduleTitre;
        private Integer totalLecons;
        private Integer leconsCompletees;
        private Float progression;
        private QuizResultatDetail quizResultat;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuizResultatDetail {
        private Long quizId;
        private String quizTitre;
        private Double meilleurScore;
        private Integer nombreTentatives;
        private Long derniereTentative;
        private Boolean passed;
    }
}
