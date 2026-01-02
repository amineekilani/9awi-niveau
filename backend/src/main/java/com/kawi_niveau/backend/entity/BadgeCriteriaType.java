package com.kawi_niveau.backend.entity;

public enum BadgeCriteriaType {
    COURS_COMPLETED,        // Nombre de cours terminés
    QUIZ_PASSED,           // Nombre de quiz réussis
    PERFECT_SCORE,         // Score parfait sur un quiz
    STREAK_DAYS,           // Jours consécutifs de connexion
    XP_EARNED,             // Points XP gagnés
    FIRST_COURSE,          // Premier cours terminé
    FIRST_QUIZ,            // Premier quiz réussi
    CHALLENGE_COMPLETED,   // Défi terminé
    LEVEL_REACHED          // Niveau atteint
}