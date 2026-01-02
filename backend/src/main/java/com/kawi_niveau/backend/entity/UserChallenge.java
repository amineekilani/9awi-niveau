package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_challenges")
@Data
public class UserChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "current_progress")
    private Integer currentProgress = 0;

    @Column(name = "is_completed")
    private boolean isCompleted = false;

    @Column(name = "completed_at")
    private Long completedAt;

    @Column(name = "joined_at")
    private Long joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = System.currentTimeMillis();
    }

    public void markCompleted() {
        this.isCompleted = true;
        this.completedAt = System.currentTimeMillis();
    }
}