package com.kawi_niveau.backend.dto;

import java.util.List;

public class FormateurStatsResponse {
    private int totalCours;
    private int coursActifs;
    private int totalApprenants;
    private double tauxReussiteMoyen;
    private List<CoursParNiveauDto> coursParNiveau;

    public FormateurStatsResponse() {}

    public FormateurStatsResponse(int totalCours, int coursActifs, int totalApprenants,
                                  double tauxReussiteMoyen, List<CoursParNiveauDto> coursParNiveau) {
        this.totalCours = totalCours;
        this.coursActifs = coursActifs;
        this.totalApprenants = totalApprenants;
        this.tauxReussiteMoyen = tauxReussiteMoyen;
        this.coursParNiveau = coursParNiveau;
    }

    // Getters et Setters
    public int getTotalCours() {
        return totalCours;
    }

    public void setTotalCours(int totalCours) {
        this.totalCours = totalCours;
    }

    public int getCoursActifs() {
        return coursActifs;
    }

    public void setCoursActifs(int coursActifs) {
        this.coursActifs = coursActifs;
    }

    public int getTotalApprenants() {
        return totalApprenants;
    }

    public void setTotalApprenants(int totalApprenants) {
        this.totalApprenants = totalApprenants;
    }

    public double getTauxReussiteMoyen() {
        return tauxReussiteMoyen;
    }

    public void setTauxReussiteMoyen(double tauxReussiteMoyen) {
        this.tauxReussiteMoyen = tauxReussiteMoyen;
    }

    public List<CoursParNiveauDto> getCoursParNiveau() {
        return coursParNiveau;
    }

    public void setCoursParNiveau(List<CoursParNiveauDto> coursParNiveau) {
        this.coursParNiveau = coursParNiveau;
    }
}