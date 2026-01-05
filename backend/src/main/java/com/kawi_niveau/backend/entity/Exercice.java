package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercice")
@Data
public class Exercice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_exercice", nullable = false)
    private TypeExercice typeExercice;

    @OneToOne
    @JoinColumn(name = "module_id", nullable = false)
    private com.kawi_niveau.backend.entity.Module module;

    @OneToMany(mappedBy = "exercice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionOrdre ASC")
    private List<ExerciceElement> elements = new ArrayList<>();

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    public enum TypeExercice {
        FILL_BLANK,    // Texte à trous
        DRAG_DROP,     // Glisser-déposer
        MATCHING       // Appariement
    }
}