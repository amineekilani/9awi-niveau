package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cours")
@Data
public class Cours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "archived")
    private boolean archived = false;

    @Column(name = "archived_at")
    private Long archivedAt;

    @Column(name = "categorie", length = 100)
    private String categorie;

    @ManyToOne
    @JoinColumn(name = "formateur_id", nullable = false)
    private User formateur;

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
