package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.TypeParcours;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ParcoursRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    private String thumbnailUrl;

    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String categorie;

    private NiveauDifficulte niveauDifficulte;

    private Integer dureeEstimeeHeures;

    @Size(max = 1000, message = "Les prérequis ne peuvent pas dépasser 1000 caractères")
    private String prerequis;

    @NotNull(message = "Le type de parcours est obligatoire")
    private TypeParcours typeParcours = TypeParcours.LINEAIRE;

    private Integer pointsBonus = 0;

    private String badgeCompletion;

    private Boolean certificatEnabled = false;

    private Boolean isPublished = false;

    private List<ParcoursEtapeRequest> etapes;

    // Constructeurs
    public ParcoursRequest() {}

    // Getters et Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public NiveauDifficulte getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(NiveauDifficulte niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }

    public Integer getDureeEstimeeHeures() { return dureeEstimeeHeures; }
    public void setDureeEstimeeHeures(Integer dureeEstimeeHeures) { this.dureeEstimeeHeures = dureeEstimeeHeures; }

    public String getPrerequis() { return prerequis; }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; }

    public TypeParcours getTypeParcours() { return typeParcours; }
    public void setTypeParcours(TypeParcours typeParcours) { this.typeParcours = typeParcours; }

    public Integer getPointsBonus() { return pointsBonus; }
    public void setPointsBonus(Integer pointsBonus) { this.pointsBonus = pointsBonus; }

    public String getBadgeCompletion() { return badgeCompletion; }
    public void setBadgeCompletion(String badgeCompletion) { this.badgeCompletion = badgeCompletion; }

    public Boolean getCertificatEnabled() { return certificatEnabled; }
    public void setCertificatEnabled(Boolean certificatEnabled) { this.certificatEnabled = certificatEnabled; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public List<ParcoursEtapeRequest> getEtapes() { return etapes; }
    public void setEtapes(List<ParcoursEtapeRequest> etapes) { this.etapes = etapes; }
}