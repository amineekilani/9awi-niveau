package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "challenges")
@Data
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "challenge_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeType challengeType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "is_active", columnDefinition = "TINYINT(1)")
    private Boolean isActive = true;

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
}