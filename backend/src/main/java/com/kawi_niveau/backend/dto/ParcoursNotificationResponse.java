package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.ParcoursNotification;
import java.time.LocalDateTime;

public class ParcoursNotificationResponse {
    
    private Long id;
    private String type;
    private String title;
    private String message;
    private Integer xpEarned;
    private Boolean certificateReady;
    private String certificateUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    // Informations du parcours
    private Long parcoursId;
    private String parcoursTitle;
    private String parcoursDescription;

    // Constructeurs
    public ParcoursNotificationResponse() {}

    public ParcoursNotificationResponse(ParcoursNotification notification) {
        this.id = notification.getId();
        this.type = notification.getType().name();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.xpEarned = notification.getXpEarned();
        this.certificateReady = notification.getCertificateReady();
        this.certificateUrl = notification.getCertificateUrl();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
        
        if (notification.getParcours() != null) {
            this.parcoursId = notification.getParcours().getId();
            this.parcoursTitle = notification.getParcours().getTitre();
            this.parcoursDescription = notification.getParcours().getDescription();
        }
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

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

    public Long getParcoursId() { return parcoursId; }
    public void setParcoursId(Long parcoursId) { this.parcoursId = parcoursId; }

    public String getParcoursTitle() { return parcoursTitle; }
    public void setParcoursTitle(String parcoursTitle) { this.parcoursTitle = parcoursTitle; }

    public String getParcoursDescription() { return parcoursDescription; }
    public void setParcoursDescription(String parcoursDescription) { this.parcoursDescription = parcoursDescription; }
}