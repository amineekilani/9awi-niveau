package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "level_notifications")
@Data
public class LevelNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "old_level", nullable = false)
    private Integer oldLevel;

    @Column(name = "new_level", nullable = false)
    private Integer newLevel;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @Column(name = "total_xp", nullable = false)
    private Integer totalXP;

    @Column(name = "xp_gained", nullable = false)
    private Integer xpGained;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_new", nullable = false)
    private Boolean isNew = true;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
}