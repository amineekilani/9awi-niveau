package com.kawi_niveau.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ApprenantProgressionResponse {
    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private LocalDateTime dateInscription;
    private LocalDateTime dateCompletion;
    private Integer progressionPourcentage;
    private Integer etapeCourante;
    private Integer totalEtapes;
    private Integer pointsGagnes;
    private Boolean isCompleted;
    private Boolean certificatGenere;
    private String certificatUrl;
    private String statut; // "En cours", "Terminé", "Non commencé"
    private List<EtapeProgressionDto> etapesProgression;

    // Constructeurs
    public ApprenantProgressionResponse() {}

    public ApprenantProgressionResponse(Long userId, String nom, String prenom, String email,
                                      LocalDateTime dateInscription, LocalDateTime dateCompletion,
                                      Integer progressionPourcentage, Integer etapeCourante,
                                      Integer totalEtapes, Integer pointsGagnes,
                                      Boolean isCompleted, Boolean certificatGenere,
                                      String certificatUrl) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateInscription = dateInscription;
        this.dateCompletion = dateCompletion;
        this.progressionPourcentage = progressionPourcentage;
        this.etapeCourante = etapeCourante;
        this.totalEtapes = totalEtapes;
        this.pointsGagnes = pointsGagnes;
        this.isCompleted = isCompleted;
        this.certificatGenere = certificatGenere;
        this.certificatUrl = certificatUrl;
        
        // Déterminer le statut
        if (isCompleted) {
            this.statut = "Terminé";
        } else if (progressionPourcentage > 0) {
            this.statut = "En cours";
        } else {
            this.statut = "Non commencé";
        }
    }

    // Getters et Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public LocalDateTime getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDateTime dateCompletion) { this.dateCompletion = dateCompletion; }

    public Integer getProgressionPourcentage() { return progressionPourcentage; }
    public void setProgressionPourcentage(Integer progressionPourcentage) { 
        this.progressionPourcentage = progressionPourcentage; 
    }

    public Integer getEtapeCourante() { return etapeCourante; }
    public void setEtapeCourante(Integer etapeCourante) { this.etapeCourante = etapeCourante; }

    public Integer getTotalEtapes() { return totalEtapes; }
    public void setTotalEtapes(Integer totalEtapes) { this.totalEtapes = totalEtapes; }

    public Integer getPointsGagnes() { return pointsGagnes; }
    public void setPointsGagnes(Integer pointsGagnes) { this.pointsGagnes = pointsGagnes; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Boolean getCertificatGenere() { return certificatGenere; }
    public void setCertificatGenere(Boolean certificatGenere) { this.certificatGenere = certificatGenere; }

    public String getCertificatUrl() { return certificatUrl; }
    public void setCertificatUrl(String certificatUrl) { this.certificatUrl = certificatUrl; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public List<EtapeProgressionDto> getEtapesProgression() { return etapesProgression; }
    public void setEtapesProgression(List<EtapeProgressionDto> etapesProgression) { 
        this.etapesProgression = etapesProgression; 
    }

    // Classe interne pour les détails des étapes
    public static class EtapeProgressionDto {
        private Long etapeId;
        private String titreCours;
        private Integer ordreEtape;
        private Boolean isCompleted;
        private Integer scoreObtenu;
        private LocalDateTime dateCompletion;

        public EtapeProgressionDto() {}

        public EtapeProgressionDto(Long etapeId, String titreCours, Integer ordreEtape,
                                 Boolean isCompleted, Integer scoreObtenu, LocalDateTime dateCompletion) {
            this.etapeId = etapeId;
            this.titreCours = titreCours;
            this.ordreEtape = ordreEtape;
            this.isCompleted = isCompleted;
            this.scoreObtenu = scoreObtenu;
            this.dateCompletion = dateCompletion;
        }

        // Getters et Setters
        public Long getEtapeId() { return etapeId; }
        public void setEtapeId(Long etapeId) { this.etapeId = etapeId; }

        public String getTitreCours() { return titreCours; }
        public void setTitreCours(String titreCours) { this.titreCours = titreCours; }

        public Integer getOrdreEtape() { return ordreEtape; }
        public void setOrdreEtape(Integer ordreEtape) { this.ordreEtape = ordreEtape; }

        public Boolean getIsCompleted() { return isCompleted; }
        public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

        public Integer getScoreObtenu() { return scoreObtenu; }
        public void setScoreObtenu(Integer scoreObtenu) { this.scoreObtenu = scoreObtenu; }

        public LocalDateTime getDateCompletion() { return dateCompletion; }
        public void setDateCompletion(LocalDateTime dateCompletion) { this.dateCompletion = dateCompletion; }
    }

    // Classe interne pour les détails de progression des modules (utilisée par EnrollmentService)
    public static class ModuleProgressionDetail {
        private Long moduleId;
        private String titreModule;
        private Integer ordreModule;
        private Float progressionPourcentage;
        private Integer totalLecons;
        private Integer leconsCompletees;
        private QuizResultatDetail quizDetail;

        public ModuleProgressionDetail() {}

        public ModuleProgressionDetail(Long moduleId, String titreModule, Integer ordreModule,
                                     Float progressionPourcentage, Integer totalLecons, 
                                     Integer leconsCompletees, QuizResultatDetail quizDetail) {
            this.moduleId = moduleId;
            this.titreModule = titreModule;
            this.ordreModule = ordreModule;
            this.progressionPourcentage = progressionPourcentage;
            this.totalLecons = totalLecons;
            this.leconsCompletees = leconsCompletees;
            this.quizDetail = quizDetail;
        }

        // Getters et Setters
        public Long getModuleId() { return moduleId; }
        public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

        public String getTitreModule() { return titreModule; }
        public void setTitreModule(String titreModule) { this.titreModule = titreModule; }

        public Integer getOrdreModule() { return ordreModule; }
        public void setOrdreModule(Integer ordreModule) { this.ordreModule = ordreModule; }

        public Float getProgressionPourcentage() { return progressionPourcentage; }
        public void setProgressionPourcentage(Float progressionPourcentage) { this.progressionPourcentage = progressionPourcentage; }

        public Integer getTotalLecons() { return totalLecons; }
        public void setTotalLecons(Integer totalLecons) { this.totalLecons = totalLecons; }

        public Integer getLeconsCompletees() { return leconsCompletees; }
        public void setLeconsCompletees(Integer leconsCompletees) { this.leconsCompletees = leconsCompletees; }

        public QuizResultatDetail getQuizDetail() { return quizDetail; }
        public void setQuizDetail(QuizResultatDetail quizDetail) { this.quizDetail = quizDetail; }
    }

    // Classe interne pour les détails des résultats de quiz (utilisée par EnrollmentService)
    public static class QuizResultatDetail {
        private Long quizId;
        private String titreQuiz;
        private Double meilleurScore;
        private Integer nombreTentatives;
        private LocalDateTime dateDerniereTentative;
        private Boolean reussi;

        public QuizResultatDetail() {}

        public QuizResultatDetail(Long quizId, String titreQuiz, Double meilleurScore,
                                Integer nombreTentatives, LocalDateTime dateDerniereTentative,
                                Boolean reussi) {
            this.quizId = quizId;
            this.titreQuiz = titreQuiz;
            this.meilleurScore = meilleurScore;
            this.nombreTentatives = nombreTentatives;
            this.dateDerniereTentative = dateDerniereTentative;
            this.reussi = reussi;
        }

        // Getters et Setters
        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }

        public String getTitreQuiz() { return titreQuiz; }
        public void setTitreQuiz(String titreQuiz) { this.titreQuiz = titreQuiz; }

        public Double getMeilleurScore() { return meilleurScore; }
        public void setMeilleurScore(Double meilleurScore) { this.meilleurScore = meilleurScore; }

        public Integer getNombreTentatives() { return nombreTentatives; }
        public void setNombreTentatives(Integer nombreTentatives) { this.nombreTentatives = nombreTentatives; }

        public LocalDateTime getDateDerniereTentative() { return dateDerniereTentative; }
        public void setDateDerniereTentative(LocalDateTime dateDerniereTentative) { this.dateDerniereTentative = dateDerniereTentative; }

        public Boolean getReussi() { return reussi; }
        public void setReussi(Boolean reussi) { this.reussi = reussi; }
    }
} 