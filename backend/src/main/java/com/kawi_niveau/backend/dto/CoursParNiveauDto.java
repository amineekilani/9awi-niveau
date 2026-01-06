package com.kawi_niveau.backend.dto;

public class CoursParNiveauDto {
    private String niveau;
    private int nombre;
    private double pourcentage;

    public CoursParNiveauDto() {}

    public CoursParNiveauDto(String niveau, int nombre, double pourcentage) {
        this.niveau = niveau;
        this.nombre = nombre;
        this.pourcentage = pourcentage;
    }

    // Getters et Setters
    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getNombre() {
        return nombre;
    }

    public void setNombre(int nombre) {
        this.nombre = nombre;
    }

    public double getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(double pourcentage) {
        this.pourcentage = pourcentage;
    }
}