package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "resultat_quiz")
@Data
public class ResultatQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "score", nullable = false)
    private Double score; // Score en pourcentage (0-100)

    @Column(name = "date_passed", nullable = false)
    private Long datePassed;

    @Column(name = "nombre_questions")
    private Integer nombreQuestions;

    @Column(name = "reponses_correctes")
    private Integer reponsesCorrectes;

    @Column(name = "temps_passe") // en secondes
    private Integer tempsPasse;

    @PrePersist
    protected void onCreate() {
        datePassed = System.currentTimeMillis();
    }
}
