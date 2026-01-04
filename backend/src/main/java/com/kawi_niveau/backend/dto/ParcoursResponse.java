package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.TypeParcours;
import java.time.LocalDateTime;
import java.util.List;

public class ParcoursResponse {
    private Long id;
    private String titre;
    private String description;
    private String thumbnailUrl;
    private String categorie;
    private NiveauDifficulte niveauDifficulte;
    private Integer dureeEstimeeHeures;
    private String prerequis;
    private TypeParcours typeParcours;
    private Integer pointsBonus;
    private String badgeCompletion;
    private Boolean certificatEnabled;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations du formateur
    private String formateurNom;
    private String formateurEmail;
    
    // Statistiques
    private Integer nombreEtapes;
    private Integer nombreInscriptions;
    private Integer nombreCompletions;
    private Double progressionMoyenne;
    
    // Étapes du parcours
    private List<ParcoursEtapeResponse> etapes;
    
    // Pour l'apprenant connecté
    private Boolean isInscrit = false;
    private Integer progressionUtilisateur;
    private Integer etapeCouranteUtilisateur;

    // Constructeurs
    public ParcoursResponse() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFormateurNom() { return formateurNom; }
    public void setFormateurNom(String formateurNom) { this.formateurNom = formateurNom; }

    public String getFormateurEmail() { return formateurEmail; }
    public void setFormateurEmail(String formateurEmail) { this.formateurEmail = formateurEmail; }

    public Integer getNombreEtapes() { return nombreEtapes; }
    public void setNombreEtapes(Integer nombreEtapes) { this.nombreEtapes = nombreEtapes; }

    public Integer getNombreInscriptions() { return nombreInscriptions; }
    public void setNombreInscriptions(Integer nombreInscriptions) { this.nombreInscriptions = nombreInscriptions; }

    public Integer getNombreCompletions() { return nombreCompletions; }
    public void setNombreCompletions(Integer nombreCompletions) { this.nombreCompletions = nombreCompletions; }

    public Double getProgressionMoyenne() { return progressionMoyenne; }
    public void setProgressionMoyenne(Double progressionMoyenne) { this.progressionMoyenne = progressionMoyenne; }

    public List<ParcoursEtapeResponse> getEtapes() { return etapes; }
    public void setEtapes(List<ParcoursEtapeResponse> etapes) { this.etapes = etapes; }

    public Boolean getIsInscrit() { return isInscrit; }
    public void setIsInscrit(Boolean isInscrit) { this.isInscrit = isInscrit; }

    public Integer getProgressionUtilisateur() { return progressionUtilisateur; }
    public void setProgressionUtilisateur(Integer progressionUtilisateur) { this.progressionUtilisateur = progressionUtilisateur; }

    public Integer getEtapeCouranteUtilisateur() { return etapeCouranteUtilisateur; }
    public void setEtapeCouranteUtilisateur(Integer etapeCouranteUtilisateur) { this.etapeCouranteUtilisateur = etapeCouranteUtilisateur; }
}