package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "parcours_etapes")
public class ParcoursEtape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id", nullable = false)
    private ParcoursApprentissage parcours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @Column(name = "ordre_etape", nullable = false)
    private Integer ordreEtape;

    @Column(name = "niveau_etape")
    private Integer niveauEtape = 1; // 1=Fondamental, 2=Intermédiaire, 3=Avancé

    @Column(name = "is_obligatoire")
    private Boolean isObligatoire = true;

    @Column(name = "score_minimum")
    private Integer scoreMinimum = 0;

    @Column(name = "pourcentage_completion_requis")
    private Integer pourcentageCompletionRequis = 100;

    @Column(name = "quiz_obligatoires")
    private Boolean quizObligatoires = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "etape", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParcoursCondition> conditions;

    // Constructeurs
    public ParcoursEtape() {}

    public ParcoursEtape(ParcoursApprentissage parcours, Cours cours, Integer ordreEtape) {
        this.parcours = parcours;
        this.cours = cours;
        this.ordreEtape = ordreEtape;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParcoursApprentissage getParcours() { return parcours; }
    public void setParcours(ParcoursApprentissage parcours) { this.parcours = parcours; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

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

    public List<ParcoursCondition> getConditions() { return conditions; }
    public void setConditions(List<ParcoursCondition> conditions) { this.conditions = conditions; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}