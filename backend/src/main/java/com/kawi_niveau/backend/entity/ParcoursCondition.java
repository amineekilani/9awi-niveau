package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcours_conditions")
public class ParcoursCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etape_id", nullable = false)
    private ParcoursEtape etape;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etape_prerequise_id")
    private ParcoursEtape etapePrerequisе;

    @Column(name = "type_condition")
    @Enumerated(EnumType.STRING)
    private TypeCondition typeCondition;

    @Column(name = "valeur_requise")
    private Integer valeurRequise;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructeurs
    public ParcoursCondition() {}

    public ParcoursCondition(ParcoursEtape etape, TypeCondition typeCondition, Integer valeurRequise) {
        this.etape = etape;
        this.typeCondition = typeCondition;
        this.valeurRequise = valeurRequise;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParcoursEtape getEtape() { return etape; }
    public void setEtape(ParcoursEtape etape) { this.etape = etape; }

    public ParcoursEtape getEtapePrerequisе() { return etapePrerequisе; }
    public void setEtapePrerequisе(ParcoursEtape etapePrerequisе) { this.etapePrerequisе = etapePrerequisе; }

    public TypeCondition getTypeCondition() { return typeCondition; }
    public void setTypeCondition(TypeCondition typeCondition) { this.typeCondition = typeCondition; }

    public Integer getValeurRequise() { return valeurRequise; }
    public void setValeurRequise(Integer valeurRequise) { this.valeurRequise = valeurRequise; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}