package com.kawi_niveau.backend.entity;

public enum TypeCondition {
    SCORE_MINIMUM("Score minimum requis"),
    POURCENTAGE_COMPLETION("Pourcentage de completion requis"),
    QUIZ_REUSSI("Quiz réussi"),
    ETAPE_PRECEDENTE("Étape précédente terminée"),
    TEMPS_MINIMUM("Temps minimum passé sur le cours");

    private final String description;

    TypeCondition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}