package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lecons")
@Data
public class Lecon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contenu", nullable = false)
    private TypeContenu typeContenu;

    @Column(columnDefinition = "TEXT")
    private String contenuTexte; // Pour le type TEXTE

    @Column(name = "fichier_url")
    private String fichierUrl; // Pour PDF, IMAGE, VIDEO

    @Column(name = "ordre")
    private Integer ordre;

    @Column(name = "duree")
    private Integer duree; // Durée en minutes (optionnel)

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}
