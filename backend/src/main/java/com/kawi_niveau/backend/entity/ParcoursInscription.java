package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcours_inscriptions")
public class ParcoursInscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id", nullable = false)
    private ParcoursApprentissage parcours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;

    @Column(name = "progression_pourcentage")
    private Integer progressionPourcentage = 0;

    @Column(name = "etape_courante")
    private Integer etapeCourante = 1;

    @Column(name = "points_gagnes")
    private Integer pointsGagnes = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "certificat_genere")
    private Boolean certificatGenere = false;

    @Column(name = "certificat_url")
    private String certificatUrl;

    // Constructeurs
    public ParcoursInscription() {}

    public ParcoursInscription(ParcoursApprentissage parcours, User user) {
        this.parcours = parcours;
        this.user = user;
        this.dateInscription = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParcoursApprentissage getParcours() { return parcours; }
    public void setParcours(ParcoursApprentissage parcours) { this.parcours = parcours; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public LocalDateTime getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDateTime dateCompletion) { this.dateCompletion = dateCompletion; }

    public Integer getProgressionPourcentage() { return progressionPourcentage; }
    public void setProgressionPourcentage(Integer progressionPourcentage) { 
        this.progressionPourcentage = progressionPourcentage; 
    }

    public Integer getEtapeCourante() { return etapeCourante; }
    public void setEtapeCourante(Integer etapeCourante) { this.etapeCourante = etapeCourante; }

    public Integer getPointsGagnes() { return pointsGagnes; }
    public void setPointsGagnes(Integer pointsGagnes) { this.pointsGagnes = pointsGagnes; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Boolean getCertificatGenere() { return certificatGenere; }
    public void setCertificatGenere(Boolean certificatGenere) { this.certificatGenere = certificatGenere; }

    public String getCertificatUrl() { return certificatUrl; }
    public void setCertificatUrl(String certificatUrl) { this.certificatUrl = certificatUrl; }

    @PrePersist
    protected void onCreate() {
        dateInscription = LocalDateTime.now();
    }
}