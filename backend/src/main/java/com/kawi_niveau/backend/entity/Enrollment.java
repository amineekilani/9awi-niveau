package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "cours_id"})
})
@Data
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @Column(name = "enrolled_at", nullable = false)
    private Long enrolledAt;

    @Column(name = "progress", nullable = false)
    private Float progress = 0.0f; // Pourcentage de 0 à 100

    @Column(name = "last_accessed_at")
    private Long lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        enrolledAt = System.currentTimeMillis();
        lastAccessedAt = System.currentTimeMillis();
    }
}
