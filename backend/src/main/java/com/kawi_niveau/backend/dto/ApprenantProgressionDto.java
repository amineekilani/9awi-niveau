package com.kawi_niveau.backend.dto;

public class ApprenantProgressionDto {
    private Long apprenantId;
    private String nom;
    private String prenom;
    private String email;
    private double progressionPourcentage;
    private boolean isCompleted;
    private boolean certificatGenere;
    private String dateInscription;
    private String derniereActivite;

    public ApprenantProgressionDto() {}

    public ApprenantProgressionDto(Long apprenantId, String nom, String prenom, String email,
                                   double progressionPourcentage, boolean isCompleted,
                                   boolean certificatGenere, String dateInscription, String derniereActivite) {
        this.apprenantId = apprenantId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.progressionPourcentage = progressionPourcentage;
        this.isCompleted = isCompleted;
        this.certificatGenere = certificatGenere;
        this.dateInscription = dateInscription;
        this.derniereActivite = derniereActivite;
    }

    // Getters et Setters
    public Long getApprenantId() {
        return apprenantId;
    }

    public void setApprenantId(Long apprenantId) {
        this.apprenantId = apprenantId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getProgressionPourcentage() {
        return progressionPourcentage;
    }

    public void setProgressionPourcentage(double progressionPourcentage) {
        this.progressionPourcentage = progressionPourcentage;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isCertificatGenere() {
        return certificatGenere;
    }

    public void setCertificatGenere(boolean certificatGenere) {
        this.certificatGenere = certificatGenere;
    }

    public String getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(String dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getDerniereActivite() {
        return derniereActivite;
    }

    public void setDerniereActivite(String derniereActivite) {
        this.derniereActivite = derniereActivite;
    }
}