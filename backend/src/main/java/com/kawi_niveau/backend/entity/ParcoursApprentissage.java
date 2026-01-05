package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "parcours_apprentissage")
public class ParcoursApprentissage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(length = 100)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_difficulte")
    private NiveauDifficulte niveauDifficulte;

    @Column(name = "duree_estimee_heures")
    private Integer dureeEstimeeHeures;

    @Column(columnDefinition = "TEXT")
    private String prerequis;

    @Column(name = "type_parcours")
    @Enumerated(EnumType.STRING)
    private TypeParcours typeParcours = TypeParcours.LINEAIRE;

    @Column(name = "points_bonus")
    private Integer pointsBonus = 0;

    @Column(name = "certificat_enabled")
    private Boolean certificatEnabled = false;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formateur_id", nullable = false)
    private User formateur;

    @OneToMany(mappedBy = "parcours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParcoursEtape> etapes;

    @OneToMany(mappedBy = "parcours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParcoursInscription> inscriptions;

    // Constructeurs
    public ParcoursApprentissage() {}

    public ParcoursApprentissage(String titre, String description, User formateur) {
        this.titre = titre;
        this.description = description;
        this.formateur = formateur;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

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

    public Boolean getCertificatEnabled() { return certificatEnabled; }
    public void setCertificatEnabled(Boolean certificatEnabled) { this.certificatEnabled = certificatEnabled; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getFormateur() { return formateur; }
    public void setFormateur(User formateur) { this.formateur = formateur; }

    public List<ParcoursEtape> getEtapes() { return etapes; }
    public void setEtapes(List<ParcoursEtape> etapes) { this.etapes = etapes; }

    public List<ParcoursInscription> getInscriptions() { return inscriptions; }
    public void setInscriptions(List<ParcoursInscription> inscriptions) { this.inscriptions = inscriptions; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}