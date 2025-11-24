package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lecon_completions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"enrollment_id", "lecon_id"})
})
@Data
public class LeconCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne
    @JoinColumn(name = "lecon_id", nullable = false)
    private Lecon lecon;

    @Column(name = "completed_at", nullable = false)
    private Long completedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = System.currentTimeMillis();
    }
}
