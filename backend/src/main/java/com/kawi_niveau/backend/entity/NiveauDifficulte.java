package com.kawi_niveau.backend.entity;

public enum NiveauDifficulte {
    DEBUTANT("Débutant"),
    INTERMEDIAIRE("Intermédiaire"),
    AVANCE("Avancé"),
    EXPERT("Expert");

    private final String displayName;

    NiveauDifficulte(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}