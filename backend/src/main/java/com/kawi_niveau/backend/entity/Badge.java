package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "badges")
@Data
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "criteria_type")
    @Enumerated(EnumType.STRING)
    private BadgeCriteriaType criteriaType;

    @Column(name = "criteria_value")
    private Integer criteriaValue;

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