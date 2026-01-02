package com.kawi_niveau.backend.entity;

public enum ChallengeType {
    COMPLETE_COURSES,       // Terminer X cours (total cumulé)
    PASS_QUIZZES,          // Réussir X quiz (total cumulé)
    PERFECT_SCORES,        // Obtenir X scores parfaits
    DAILY_LOGIN,           // Se connecter X jours consécutifs
    EARN_BADGES,           // Gagner X badges différents
    COMPLETE_MODULE        // Terminer X modules complets (toutes leçons + quiz)
}