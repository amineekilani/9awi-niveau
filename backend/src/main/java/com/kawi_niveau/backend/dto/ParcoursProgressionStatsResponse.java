package com.kawi_niveau.backend.dto;

public class ParcoursProgressionStatsResponse {
    private Long parcoursId;
    private String parcoursTitre;
    private int totalInscrits;
    private int nombreTermines;
    private int nombreEnCours;
    private int nombreCertificats;
    private double progressionMoyenne;

    public ParcoursProgressionStatsResponse() {}

    public ParcoursProgressionStatsResponse(Long parcoursId, String parcoursTitre, int totalInscrits,
                                            int nombreTermines, int nombreEnCours, int nombreCertificats,
                                            double progressionMoyenne) {
        this.parcoursId = parcoursId;
        this.parcoursTitre = parcoursTitre;
        this.totalInscrits = totalInscrits;
        this.nombreTermines = nombreTermines;
        this.nombreEnCours = nombreEnCours;
        this.nombreCertificats = nombreCertificats;
        this.progressionMoyenne = progressionMoyenne;
    }

    // Getters et Setters
    public Long getParcoursId() {
        return parcoursId;
    }

    public void setParcoursId(Long parcoursId) {
        this.parcoursId = parcoursId;
    }

    public String getParcoursTitre() {
        return parcoursTitre;
    }

    public void setParcoursTitre(String parcoursTitre) {
        this.parcoursTitre = parcoursTitre;
    }

    public int getTotalInscrits() {
        return totalInscrits;
    }

    public void setTotalInscrits(int totalInscrits) {
        this.totalInscrits = totalInscrits;
    }

    public int getNombreTermines() {
        return nombreTermines;
    }

    public void setNombreTermines(int nombreTermines) {
        this.nombreTermines = nombreTermines;
    }

    public int getNombreEnCours() {
        return nombreEnCours;
    }

    public void setNombreEnCours(int nombreEnCours) {
        this.nombreEnCours = nombreEnCours;
    }

    public int getNombreCertificats() {
        return nombreCertificats;
    }

    public void setNombreCertificats(int nombreCertificats) {
        this.nombreCertificats = nombreCertificats;
    }

    public double getProgressionMoyenne() {
        return progressionMoyenne;
    }

    public void setProgressionMoyenne(double progressionMoyenne) {
        this.progressionMoyenne = progressionMoyenne;
    }
}