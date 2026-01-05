package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcours_notifications")
public class ParcoursNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id", nullable = false)
    private ParcoursApprentissage parcours;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "xp_earned")
    private Integer xpEarned;

    @Column(name = "certificate_ready")
    private Boolean certificateReady = false;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructeurs
    public ParcoursNotification() {}

    public ParcoursNotification(User user, ParcoursApprentissage parcours, NotificationType type, 
                               String title, String message, Integer xpEarned) {
        this.user = user;
        this.parcours = parcours;
        this.type = type;
        this.title = title;
        this.message = message;
        this.xpEarned = xpEarned;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ParcoursApprentissage getParcours() { return parcours; }
    public void setParcours(ParcoursApprentissage parcours) { this.parcours = parcours; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getXpEarned() { return xpEarned; }
    public void setXpEarned(Integer xpEarned) { this.xpEarned = xpEarned; }

    public Boolean getCertificateReady() { return certificateReady; }
    public void setCertificateReady(Boolean certificateReady) { this.certificateReady = certificateReady; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Enum pour les types de notifications
    public enum NotificationType {
        PARCOURS_COMPLETED,
        CERTIFICATE_READY,
        MILESTONE_REACHED
    }
}