package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.ResultatQuiz;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.entity.Quiz;
import com.kawi_niveau.backend.entity.Module;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.dto.RecommendationResponse;
import com.kawi_niveau.backend.dto.Recommendation;
import com.kawi_niveau.backend.dto.UserLearningProfile;
import com.kawi_niveau.backend.event.RecommendationUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de recommandation pédagogique intégré
 * Utilise le moteur Python ML pour générer des recommandations personnalisées
 */
@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache simple des recommandations
    private final Map<Long, RecommendationResponse> recommendationCache = new ConcurrentHashMap<>();
    private final Map<Long, Long> cacheTimestamps = new ConcurrentHashMap<>();

    // Durée de vie du cache (30 minutes)
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000;

    /**
     * Génère des recommandations personnalisées pour un utilisateur
     */
    public RecommendationResponse generateRecommendations(Long userId) {
        try {
            logger.info("Génération de recommandations pour l'utilisateur {}", userId);

            // Vérifier le cache d'abord
            RecommendationResponse cachedResponse = getCachedRecommendations(userId);
            if (cachedResponse != null) {
                logger.debug("Recommandations servies depuis le cache pour l'utilisateur {}", userId);
                return cachedResponse;
            }

            // 1. Récupérer les données utilisateur
            UserLearningProfile profile = buildUserProfile(userId);

            // 2. Appliquer les règles pédagogiques
            List<Recommendation> recommendations = applyPedagogicalRules(profile);

            // 3. Enrichir avec des recommandations intelligentes (basées sur les intérêts)
            List<Recommendation> smartRecommendations = getSmartRecommendations(profile);
            recommendations = mergeRecommendations(recommendations, smartRecommendations);

            // 4. Finaliser et trier les recommandations
            recommendations = finalizeRecommendations(recommendations, profile);

            RecommendationResponse response = RecommendationResponse.builder()
                    .userId(userId)
                    .generatedAt(Instant.now().toString())
                    .recommendations(recommendations)
                    .build();

            // 5. Mettre en cache
            cacheRecommendations(userId, response);

            return response;

        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations pour l'utilisateur {}", userId, e);
            return getDefaultRecommendations(userId);
        }
    }

    /**
     * Invalide le cache des recommandations pour un utilisateur
     */
    public void invalidateRecommendationsCache(Long userId, String reason) {
        logger.info("Invalidation du cache de recommandations pour l'utilisateur {} - Raison: {}", userId, reason);
        recommendationCache.remove(userId);
        cacheTimestamps.remove(userId);

        // Publier un événement pour déclencher une mise à jour asynchrone
        eventPublisher.publishEvent(new RecommendationUpdateEvent(this, userId, "CACHE_INVALIDATED", reason));
    }

    /**
     * Déclenche une mise à jour des recommandations après une action utilisateur
     */
    public void triggerUpdateAfterUserAction(Long userId, String action, String details) {
        logger.info("Déclenchement mise à jour recommandations - Utilisateur: {}, Action: {}, Détails: {}",
                userId, action, details);

        // Invalider le cache
        invalidateRecommendationsCache(userId, action + ": " + details);

        // Pré-générer les nouvelles recommandations en arrière-plan
        try {
            generateRecommendations(userId);
        } catch (Exception e) {
            logger.warn("Erreur lors de la pré-génération des recommandations", e);
        }
    }

    /**
     * Récupère les recommandations depuis le cache si valides
     */
    private RecommendationResponse getCachedRecommendations(Long userId) {
        Long timestamp = cacheTimestamps.get(userId);
        if (timestamp == null) {
            return null;
        }

        long age = System.currentTimeMillis() - timestamp;
        if (age > CACHE_DURATION_MS) {
            // Cache expiré
            recommendationCache.remove(userId);
            cacheTimestamps.remove(userId);
            return null;
        }

        return recommendationCache.get(userId);
    }

    /**
     * Met en cache les recommandations
     */
    private void cacheRecommendations(Long userId, RecommendationResponse response) {
        recommendationCache.put(userId, response);
        cacheTimestamps.put(userId, System.currentTimeMillis());

        logger.debug("Recommandations mises en cache pour l'utilisateur {}", userId);
    }

    /**
     * Construit le profil d'apprentissage de l'utilisateur
     */
    private UserLearningProfile buildUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer les inscriptions
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);

        // Récupérer les résultats de quiz
        List<ResultatQuiz> quizResults = resultatQuizRepository.findByUserOrderByDatePassedDesc(user);

        // Récupérer les XP
        Optional<UserXP> userXP = userXPRepository.findByUser(user);

        // Analyser les performances
        Map<String, Double> categoryPerformance = analyzeCategoryPerformance(enrollments, quizResults);
        List<String> weakAreas = identifyWeakAreas(quizResults);
        List<String> strongAreas = identifyStrongAreas(quizResults);

        return UserLearningProfile.builder()
                .userId(userId)
                .currentLevel(userXP.map(UserXP::getCurrentLevel).orElse(1))
                .totalXP(userXP.map(UserXP::getTotalXP).orElse(0))
                .enrollments(enrollments)
                .quizResults(quizResults)
                .categoryPerformance(categoryPerformance)
                .weakAreas(weakAreas)
                .strongAreas(strongAreas)
                .averageQuizScore(calculateAverageQuizScore(quizResults))
                .completionRate(calculateCompletionRate(enrollments))
                .build();
    }

    /**
     * Applique les règles pédagogiques de base
     */
    private List<Recommendation> applyPedagogicalRules(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Règle 1: Cours non complétés avec progression > 0
        recommendations.addAll(recommendIncompleteCoursesWithProgress(profile));

        // Règle 2: Cours de niveau approprié
        recommendations.addAll(recommendLevelAppropriateCourses(profile));

        // Règle 3: Renforcement des zones faibles
        recommendations.addAll(recommendWeakAreaReinforcement(profile));

        // Règle 4: Progression logique dans les catégories fortes
        recommendations.addAll(recommendProgressionInStrongAreas(profile));

        // Règle 5: Défis et quiz pour motivation
        recommendations.addAll(recommendChallengesAndQuizzes(profile));

        return recommendations;
    }

    /**
     * Recommande la reprise de cours non complétés
     */
    private List<Recommendation> recommendIncompleteCoursesWithProgress(UserLearningProfile profile) {
        return profile.getEnrollments().stream()
                .filter(enrollment -> enrollment.getProgress() > 0 && enrollment.getProgress() < 100)
                .sorted((e1, e2) -> Float.compare(e2.getProgress(), e1.getProgress()))
                .limit(3)
                .map(enrollment -> Recommendation.builder()
                        .type("COURS")
                        .id(enrollment.getCours().getId())
                        .title(enrollment.getCours().getTitre())
                        .reason(String.format(
                                "Continuez votre progression (%.0f%% complété) - Ne perdez pas votre élan !",
                                enrollment.getProgress()))
                        .priority(1)
                        .confidenceScore(0.95)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Recommande des cours adaptés au niveau
     */
    private List<Recommendation> recommendLevelAppropriateCourses(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Cours déjà suivis
        Set<Long> enrolledCourseIds = profile.getEnrollments().stream()
                .map(e -> e.getCours().getId())
                .collect(Collectors.toSet());

        // Logique par niveau
        List<Cours> availableCourses = coursRepository.findByArchivedFalse();

        if (profile.getCurrentLevel() <= 2) {
            // Débutant: cours fondamentaux
            recommendations.addAll(
                    availableCourses.stream()
                            .filter(cours -> !enrolledCourseIds.contains(cours.getId()))
                            .filter(cours -> cours.getCategorie() != null &&
                                    (cours.getCategorie().toLowerCase().contains("fondamental") ||
                                            cours.getCategorie().toLowerCase().contains("introduction") ||
                                            cours.getCategorie().toLowerCase().contains("débutant")))
                            .limit(2)
                            .map(cours -> Recommendation.builder()
                                    .type("COURS")
                                    .id(cours.getId())
                                    .title(cours.getTitre())
                                    .reason("Cours fondamental adapté à votre niveau débutant")
                                    .priority(2)
                                    .confidenceScore(0.85)
                                    .build())
                            .collect(Collectors.toList()));
        } else if (profile.getCurrentLevel() <= 5) {
            // Intermédiaire
            recommendations.addAll(
                    availableCourses.stream()
                            .filter(cours -> !enrolledCourseIds.contains(cours.getId()))
                            .filter(cours -> cours.getCategorie() != null &&
                                    (cours.getCategorie().toLowerCase().contains("intermédiaire") ||
                                            cours.getCategorie().toLowerCase().contains("pratique")))
                            .limit(2)
                            .map(cours -> Recommendation.builder()
                                    .type("COURS")
                                    .id(cours.getId())
                                    .title(cours.getTitre())
                                    .reason("Cours intermédiaire pour approfondir vos connaissances")
                                    .priority(2)
                                    .confidenceScore(0.80)
                                    .build())
                            .collect(Collectors.toList()));
        } else {
            // Avancé
            recommendations.addAll(
                    availableCourses.stream()
                            .filter(cours -> !enrolledCourseIds.contains(cours.getId()))
                            .filter(cours -> cours.getCategorie() != null &&
                                    (cours.getCategorie().toLowerCase().contains("avancé") ||
                                            cours.getCategorie().toLowerCase().contains("expert")))
                            .limit(2)
                            .map(cours -> Recommendation.builder()
                                    .type("COURS")
                                    .id(cours.getId())
                                    .title(cours.getTitre())
                                    .reason("Cours avancé pour maîtriser des concepts complexes")
                                    .priority(2)
                                    .confidenceScore(0.90)
                                    .build())
                            .collect(Collectors.toList()));
        }

        return recommendations;
    }

    /**
     * Recommande du contenu pour renforcer les zones faibles
     */
    private List<Recommendation> recommendWeakAreaReinforcement(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (String weakArea : profile.getWeakAreas()) {
            // Trouver des leçons ou quiz dans cette catégorie
            List<Cours> reinforcementCourses = coursRepository.findByArchivedFalse().stream()
                    .filter(cours -> cours.getCategorie() != null &&
                            cours.getCategorie().toLowerCase().contains(weakArea.toLowerCase()))
                    .limit(1)
                    .collect(Collectors.toList());

            for (Cours cours : reinforcementCourses) {
                // Vérifier si l'utilisateur n'est pas déjà inscrit
                boolean alreadyEnrolled = profile.getEnrollments().stream()
                        .anyMatch(e -> e.getCours().getId().equals(cours.getId()));

                if (!alreadyEnrolled) {
                    recommendations.add(Recommendation.builder()
                            .type("COURS")
                            .id(cours.getId())
                            .title(cours.getTitre())
                            .reason(String.format("Recommandé pour renforcer vos compétences en %s", weakArea))
                            .priority(3)
                            .confidenceScore(0.75)
                            .build());
                }
            }
        }

        return recommendations;
    }

    /**
     * Recommande la progression dans les domaines forts
     */
    private List<Recommendation> recommendProgressionInStrongAreas(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (String strongArea : profile.getStrongAreas()) {
            // Trouver des cours plus avancés dans cette catégorie
            List<Cours> advancedCourses = coursRepository.findByArchivedFalse().stream()
                    .filter(cours -> cours.getCategorie() != null &&
                            cours.getCategorie().toLowerCase().contains(strongArea.toLowerCase()))
                    .filter(cours -> cours.getCategorie().toLowerCase().contains("avancé") ||
                            cours.getCategorie().toLowerCase().contains("expert"))
                    .limit(1)
                    .collect(Collectors.toList());

            for (Cours cours : advancedCourses) {
                boolean alreadyEnrolled = profile.getEnrollments().stream()
                        .anyMatch(e -> e.getCours().getId().equals(cours.getId()));

                if (!alreadyEnrolled) {
                    recommendations.add(Recommendation.builder()
                            .type("COURS")
                            .id(cours.getId())
                            .title(cours.getTitre())
                            .reason(String.format("Excellez davantage en %s - vous maîtrisez déjà les bases !",
                                    strongArea))
                            .priority(4)
                            .confidenceScore(0.70)
                            .build());
                }
            }
        }

        return recommendations;
    }

    /**
     * Recommande des défis et quiz pour maintenir la motivation
     */
    private List<Recommendation> recommendChallengesAndQuizzes(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Trouver des quiz non tentés dans les cours suivis
        for (Enrollment enrollment : profile.getEnrollments()) {
            List<Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(enrollment.getCours());

            for (Module module : modules) {
                Optional<Quiz> quiz = quizRepository.findByModule(module);

                if (quiz.isPresent()) {
                    // Vérifier si l'utilisateur a déjà tenté ce quiz
                    boolean alreadyAttempted = profile.getQuizResults().stream()
                            .anyMatch(result -> result.getQuiz().getId().equals(quiz.get().getId()));

                    if (!alreadyAttempted) {
                        recommendations.add(Recommendation.builder()
                                .type("QUIZ")
                                .id(quiz.get().getId())
                                .title(quiz.get().getTitre())
                                .reason("Testez vos connaissances avec ce quiz")
                                .priority(5)
                                .confidenceScore(0.60)
                                .build());

                        break; // Un seul quiz par cours pour éviter la surcharge
                    }
                }
            }
        }

        return recommendations.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * Méthodes utilitaires pour l'analyse des performances
     */
    private Map<String, Double> analyzeCategoryPerformance(List<Enrollment> enrollments,
            List<ResultatQuiz> quizResults) {
        Map<String, List<Double>> categoryScores = new HashMap<>();

        for (ResultatQuiz result : quizResults) {
            String category = result.getQuiz().getModule().getCours().getCategorie();
            if (category != null) {
                categoryScores.computeIfAbsent(category, k -> new ArrayList<>()).add(result.getScore());
            }
        }

        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : categoryScores.entrySet()) {
            double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averages.put(entry.getKey(), average);
        }

        return averages;
    }

    private List<String> identifyWeakAreas(List<ResultatQuiz> quizResults) {
        Map<String, Double> categoryPerformance = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();

        for (ResultatQuiz result : quizResults) {
            String category = result.getQuiz().getModule().getCours().getCategorie();
            if (category != null) {
                categoryPerformance.merge(category, result.getScore(), Double::sum);
                categoryCount.merge(category, 1, Integer::sum);
            }
        }

        return categoryPerformance.entrySet().stream()
                .filter(entry -> categoryCount.get(entry.getKey()) >= 2) // Au moins 2 tentatives
                .filter(entry -> (entry.getValue() / categoryCount.get(entry.getKey())) < 60.0) // Moyenne < 60%
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> identifyStrongAreas(List<ResultatQuiz> quizResults) {
        Map<String, Double> categoryPerformance = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();

        for (ResultatQuiz result : quizResults) {
            String category = result.getQuiz().getModule().getCours().getCategorie();
            if (category != null) {
                categoryPerformance.merge(category, result.getScore(), Double::sum);
                categoryCount.merge(category, 1, Integer::sum);
            }
        }

        return categoryPerformance.entrySet().stream()
                .filter(entry -> categoryCount.get(entry.getKey()) >= 2)
                .filter(entry -> (entry.getValue() / categoryCount.get(entry.getKey())) >= 80.0) // Moyenne >= 80%
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateAverageQuizScore(List<ResultatQuiz> quizResults) {
        return quizResults.stream()
                .mapToDouble(ResultatQuiz::getScore)
                .average()
                .orElse(0.0);
    }

    private double calculateCompletionRate(List<Enrollment> enrollments) {
        if (enrollments.isEmpty())
            return 0.0;

        double totalProgress = enrollments.stream()
                .mapToDouble(Enrollment::getProgress)
                .sum();

        return totalProgress / enrollments.size();
    }

    /**
     * Génère des recommandations "intelligentes" basées sur l'analyse des intérêts
     * et des similarités de contenu (Content-Based Filtering)
     */
    private List<Recommendation> getSmartRecommendations(UserLearningProfile profile) {
        List<Recommendation> recommendations = new ArrayList<>();
        Map<String, Double> interestMap = new HashMap<>();

        // 1. Construire la carte d'intérêts basée sur les inscriptions existantes
        for (Enrollment enrollment : profile.getEnrollments()) {
            if (enrollment.getCours().getCategorie() != null) {
                String category = enrollment.getCours().getCategorie();
                double weight = 1.0;

                // Bonus pour la progression
                if (enrollment.getProgress() > 50)
                    weight += 0.5;
                if (enrollment.getProgress() >= 90)
                    weight += 0.5;

                interestMap.merge(category, weight, Double::sum);
            }
        }

        // Bonus pour les bonnes performances aux quiz
        for (ResultatQuiz result : profile.getQuizResults()) {
            String category = result.getQuiz().getModule().getCours().getCategorie();
            if (category != null && result.getScore() > 70) {
                interestMap.merge(category, 1.0, Double::sum);
            }
        }

        // 2. Trouver les cours candidats
        Set<Long> enrolledCourseIds = profile.getEnrollments().stream()
                .map(e -> e.getCours().getId())
                .collect(Collectors.toSet());

        List<Cours> candidateCourses = coursRepository.findByArchivedFalse().stream()
                .filter(c -> !enrolledCourseIds.contains(c.getId()))
                .collect(Collectors.toList());

        // 3. Scorer les candidats
        for (Cours cours : candidateCourses) {
            double score = 0.0;
            String reason = "";

            if (cours.getCategorie() != null) {
                // Correspondance exacte de catégorie
                if (interestMap.containsKey(cours.getCategorie())) {
                    score += interestMap.get(cours.getCategorie());
                    reason = "Basé sur votre intérêt pour " + cours.getCategorie();
                }

                // Correspondance partielle (mots-clés)
                for (String interest : interestMap.keySet()) {
                    if (!interest.equals(cours.getCategorie()) &&
                            (cours.getCategorie().contains(interest) || interest.contains(cours.getCategorie()))) {
                        score += interestMap.get(interest) * 0.5;
                        if (reason.isEmpty()) {
                            reason = "Similaire aux cours de " + interest + " que vous appréciez";
                        }
                    }
                }
            }

            if (score > 0) {
                // Normaliser le score entre 0.5 et 0.95
                double normalizedScore = 0.5 + (Math.min(score, 10.0) / 10.0) * 0.45;

                recommendations.add(Recommendation.builder()
                        .type("COURS")
                        .id(cours.getId())
                        .title(cours.getTitre())
                        .reason(reason)
                        .priority(2) // Priorité standard+
                        .confidenceScore(normalizedScore)
                        .build());
            }
        }

        return recommendations;
    }

    /**
     * Fusionne les recommandations de différentes sources
     */
    private List<Recommendation> mergeRecommendations(List<Recommendation> rulesBased,
            List<Recommendation> smartBased) {
        Map<String, Recommendation> merged = new HashMap<>();

        // Ajouter les recommandations basées sur les règles
        for (Recommendation rec : rulesBased) {
            String key = rec.getType() + "_" + rec.getId();
            merged.put(key, rec);
        }

        // Ajouter/fusionner les recommandations intelligentes
        for (Recommendation rec : smartBased) {
            String key = rec.getType() + "_" + rec.getId();
            if (merged.containsKey(key)) {
                // Augmenter le score de confiance si recommandé par les deux méthodes
                Recommendation existing = merged.get(key);
                double newScore = Math.min(0.99, existing.getConfidenceScore() + 0.15); // Bonus cumulatif
                existing.setConfidenceScore(newScore);

                // Garder la meilleure priorité (plus petite valeur = plus haute priorité)
                existing.setPriority(Math.min(existing.getPriority(), rec.getPriority()));

                // Enrichir la raison si nécessaire
                if (!existing.getReason().contains("Basé sur")) {
                    existing.setReason(existing.getReason() + " et " + rec.getReason().toLowerCase());
                }
            } else {
                merged.put(key, rec);
            }
        }

        return new ArrayList<>(merged.values());
    }

    /**
     * Finalise et trie les recommandations
     */
    private List<Recommendation> finalizeRecommendations(List<Recommendation> recommendations,
            UserLearningProfile profile) {
        return recommendations.stream()
                .sorted((r1, r2) -> {
                    // Trier par priorité puis par score de confiance
                    int priorityCompare = Integer.compare(r1.getPriority(), r2.getPriority());
                    if (priorityCompare != 0)
                        return priorityCompare;
                    return Double.compare(r2.getConfidenceScore(), r1.getConfidenceScore());
                })
                .limit(10) // Limiter à 10 recommandations
                .collect(Collectors.toList());
    }

    /**
     * Recommandations par défaut en cas d'erreur
     */
    private RecommendationResponse getDefaultRecommendations(Long userId) {
        List<Cours> popularCourses = coursRepository.findByArchivedFalse().stream()
                .limit(3)
                .collect(Collectors.toList());

        List<Recommendation> defaultRecs = popularCourses.stream()
                .map(cours -> Recommendation.builder()
                        .type("COURS")
                        .id(cours.getId())
                        .title(cours.getTitre())
                        .reason("Cours populaire recommandé pour débuter")
                        .priority(1)
                        .confidenceScore(0.50)
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .userId(userId)
                .generatedAt(Instant.now().toString())
                .recommendations(defaultRecs)
                .build();
    }
}
