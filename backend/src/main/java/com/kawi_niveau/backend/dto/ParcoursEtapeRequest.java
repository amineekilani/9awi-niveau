package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ParcoursEtapeRequest {
    
    @NotNull(message = "L'ID du cours est obligatoire")
    private Long coursId;

    @NotNull(message = "L'ordre de l'étape est obligatoire")
    @Min(value = 1, message = "L'ordre de l'étape doit être supérieur à 0")
    private Integer ordreEtape;

    @Min(value = 1, message = "Le niveau de l'étape doit être entre 1 et 3")
    @Max(value = 3, message = "Le niveau de l'étape doit être entre 1 et 3")
    private Integer niveauEtape = 1;

    private Boolean isObligatoire = true;

    @Min(value = 0, message = "Le score minimum doit être positif")
    @Max(value = 100, message = "Le score minimum ne peut pas dépasser 100")
    private Integer scoreMinimum = 0;

    @Min(value = 0, message = "Le pourcentage de completion doit être positif")
    @Max(value = 100, message = "Le pourcentage de completion ne peut pas dépasser 100")
    private Integer pourcentageCompletionRequis = 100;

    private Boolean quizObligatoires = false;

    private String description;

    // Constructeurs
    public ParcoursEtapeRequest() {}

    public ParcoursEtapeRequest(Long coursId, Integer ordreEtape) {
        this.coursId = coursId;
        this.ordreEtape = ordreEtape;
    }

    // Getters et Setters
    public Long getCoursId() { return coursId; }
    public void setCoursId(Long coursId) { this.coursId = coursId; }

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
}