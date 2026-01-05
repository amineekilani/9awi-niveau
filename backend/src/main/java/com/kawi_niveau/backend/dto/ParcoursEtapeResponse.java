package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;

import java.time.LocalDateTime;

public class ParcoursEtapeResponse {
    
    private Long id;
    private Long coursId;
    private String coursTitle;
    private String coursDescription;
    private String coursThumbnailUrl;
    private NiveauDifficulte coursNiveauDifficulte;
    private String coursCategorie;
    private Integer ordreEtape;
    private Integer niveauEtape;
    private Boolean isObligatoire;
    private Integer scoreMinimum;
    private Integer pourcentageCompletionRequis;
    private Boolean quizObligatoires;
    private String description;
    private LocalDateTime createdAt;

    // Informations de validation pour l'utilisateur connecté
    private Boolean isDebloque = false;
    private Boolean isComplete = false;
    private Integer progressionCours = 0;
    private Integer scoreObtenu = 0;

    // Constructeurs
    public ParcoursEtapeResponse() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCoursId() { return coursId; }
    public void setCoursId(Long coursId) { this.coursId = coursId; }

    public String getCoursTitle() { return coursTitle; }
    public void setCoursTitle(String coursTitle) { this.coursTitle = coursTitle; }

    public String getCoursDescription() { return coursDescription; }
    public void setCoursDescription(String coursDescription) { this.coursDescription = coursDescription; }

    public String getCoursThumbnailUrl() { return coursThumbnailUrl; }
    public void setCoursThumbnailUrl(String coursThumbnailUrl) { this.coursThumbnailUrl = coursThumbnailUrl; }

    public NiveauDifficulte getCoursNiveauDifficulte() { return coursNiveauDifficulte; }
    public void setCoursNiveauDifficulte(NiveauDifficulte coursNiveauDifficulte) { this.coursNiveauDifficulte = coursNiveauDifficulte; }

    public String getCoursCategorie() { return coursCategorie; }
    public void setCoursCategorie(String coursCategorie) { this.coursCategorie = coursCategorie; }

    public Integer getOrdreEtape() { return ordreEtape; }
    public void setOrdreEtape(Integer ordreEtape) { this.ordreEtape = ordreEtape; }

    public Integer getNiveauEtape() { return niveauEtape; }
    public void setNiveauEtape(Integer niveauEtape) { this.niveauEtape = niveauEtape; }

    public Boolean getIsObligatoire() { return isObligatoire; }
    public void setIsObligatoire(Boolean isObligatoire) { this.isObligatoire = isObligatoire; }

    public Integer getScoreMinimum() { return scoreMinimum; }
    public void setScoreMinimum(Integer scoreMinimum) { this.scoreMinimum = scoreMinimum; }

    public Integer getPourcentageCompletionRequis() { return pourcentageCompletionRequis; }
    public void setPourcentageCompletionRequis(Integer pourcentageCompletionRequis) { 
        this.pourcentageCompletionRequis = pourcentageCompletionRequis; 
    }

    public Boolean getQuizObligatoires() { return quizObligatoires; }
    public void setQuizObligatoires(Boolean quizObligatoires) { this.quizObligatoires = quizObligatoires; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsDebloque() { return isDebloque; }
    public void setIsDebloque(Boolean isDebloque) { this.isDebloque = isDebloque; }

    public Boolean getIsComplete() { return isComplete; }
    public void setIsComplete(Boolean isComplete) { this.isComplete = isComplete; }

    public Integer getProgressionCours() { return progressionCours; }
    public void setProgressionCours(Integer progressionCours) { this.progressionCours = progressionCours; }

    public Integer getScoreObtenu() { return scoreObtenu; }
    public void setScoreObtenu(Integer scoreObtenu) { this.scoreObtenu = scoreObtenu; }
}