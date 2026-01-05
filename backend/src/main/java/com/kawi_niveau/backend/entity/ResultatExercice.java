package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "resultat_exercice")
@Data
public class ResultatExercice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @Column(name = "score", nullable = false)
    private Double score; // Score en pourcentage (0-100)

    @Column(name = "date_passed", nullable = false)
    private Long datePassed;

    @Column(name = "nombre_elements")
    private Integer nombreElements;

    @Column(name = "reponses_correctes")
    private Integer reponsesCorrectes;

    @Column(name = "temps_passe") // en secondes
    private Integer tempsPasse;

    @Column(name = "reponses_details", columnDefinition = "TEXT")
    private String reponsesDetails; // JSON des réponses détaillées

    @PrePersist
    protected void onCreate() {
        datePassed = System.currentTimeMillis();
    }
}