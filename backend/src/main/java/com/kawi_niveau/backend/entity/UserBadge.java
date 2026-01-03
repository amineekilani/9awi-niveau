package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_badges")
@Data
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "earned_at")
    private Long earnedAt;

    @Column(name = "is_new", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isNew = true;

    @PrePersist
    protected void onCreate() {
        earnedAt = System.currentTimeMillis();
    }
}