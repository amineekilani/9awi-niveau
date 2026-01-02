package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_xp")
@Data
public class UserXP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_xp")
    private Integer totalXP = 0;

    @Column(name = "current_level")
    private Integer currentLevel = 1;

    @Column(name = "xp_to_next_level")
    private Integer xpToNextLevel = 100;

    @Column(name = "last_updated")
    private Long lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = System.currentTimeMillis();
    }
}