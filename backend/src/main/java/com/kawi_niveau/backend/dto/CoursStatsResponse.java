package com.kawi_niveau.backend.dto;

import java.util.List;

public class CoursStatsResponse {
    private Long coursId;
    private String coursTitle;
    private int totalInscrits;
    private double progressionMoyenne;
    private double tauxReussite;
    private int nombreCertificats;
    private List<ApprenantProgressionDto> apprenants;

    public CoursStatsResponse() {}

    public CoursStatsResponse(Long coursId, String coursTitle, int totalInscrits,
                              double progressionMoyenne, double tauxReussite,
                              int nombreCertificats, List<ApprenantProgressionDto> apprenants) {
        this.coursId = coursId;
        this.coursTitle = coursTitle;
        this.totalInscrits = totalInscrits;
        this.progressionMoyenne = progressionMoyenne;
        this.tauxReussite = tauxReussite;
        this.nombreCertificats = nombreCertificats;
        this.apprenants = apprenants;
    }

    // Getters et Setters
    public Long getCoursId() {
        return coursId;
    }

    public void setCoursId(Long coursId) {
        this.coursId = coursId;
    }

    public String getCoursTitle() {
        return coursTitle;
    }

    public void setCoursTitle(String coursTitle) {
        this.coursTitle = coursTitle;
    }

    public int getTotalInscrits() {
        return totalInscrits;
    }

    public void setTotalInscrits(int totalInscrits) {
        this.totalInscrits = totalInscrits;
    }

    public double getProgressionMoyenne() {
        return progressionMoyenne;
    }

    public void setProgressionMoyenne(double progressionMoyenne) {
        this.progressionMoyenne = progressionMoyenne;
    }

    public double getTauxReussite() {
        return tauxReussite;
    }

    public void setTauxReussite(double tauxReussite) {
        this.tauxReussite = tauxReussite;
    }

    public int getNombreCertificats() {
        return nombreCertificats;
    }

    public void setNombreCertificats(int nombreCertificats) {
        this.nombreCertificats = nombreCertificats;
    }

    public List<ApprenantProgressionDto> getApprenants() {
        return apprenants;
    }

    public void setApprenants(List<ApprenantProgressionDto> apprenants) {
        this.apprenants = apprenants;
    }
}