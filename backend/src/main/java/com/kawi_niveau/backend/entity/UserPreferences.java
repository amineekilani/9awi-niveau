package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_categories", columnDefinition = "TEXT")
    private String preferredCategories; // JSON array des catégories préférées

    @Column(name = "preferred_difficulty")
    @Enumerated(EnumType.STRING)
    private NiveauDifficulte preferredDifficulty;

    @Column(name = "learning_style", length = 50)
    private String learningStyle; // VISUAL, AUDITORY, KINESTHETIC, READING

    @Column(name = "time_availability_hours")
    private Integer timeAvailabilityHours; // Heures disponibles par semaine

    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals; // JSON array des objectifs

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests; // JSON array des centres d'intérêt

    @Column(name = "career_focus", length = 100)
    private String careerFocus; // Orientation professionnelle

    @Column(name = "preferred_duration_min")
    private Integer preferredDurationMin; // Durée minimale préférée (heures)

    @Column(name = "preferred_duration_max")
    private Integer preferredDurationMax; // Durée maximale préférée (heures)

    @Column(name = "challenge_preference", length = 20)
    private String challengePreference; // LOW, MEDIUM, HIGH

    @Column(name = "certification_important")
    private Boolean certificationImportant = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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