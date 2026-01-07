package com.kawi_niveau.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawi_niveau.backend.dto.CoursRecommendationResponse;
import com.kawi_niveau.backend.dto.ParcoursRecommendationResponse;
import com.kawi_niveau.backend.dto.RecommendationRequest;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoursRepository coursRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Génère des recommandations personnalisées pour un utilisateur
     */
    public List<ParcoursRecommendationResponse> getPersonalizedRecommendations(User user, Integer maxResults) {
        try {
            System.out.println("🤖 Génération de recommandations IA pour: " + user.getEmail());
            
            // 1. Récupérer le profil utilisateur
            UserProfile userProfile = buildUserProfile(user);
            
            // 2. Récupérer tous les parcours publiés non inscrits
            List<ParcoursApprentissage> availableParcours = getAvailableParcours(user);
            
            if (availableParcours.isEmpty()) {
                System.out.println("❌ Aucun parcours disponible pour recommandation");
                return new ArrayList<>();
            }
            
            System.out.println("📚 " + availableParcours.size() + " parcours disponibles pour analyse");
            
            // 3. Calculer le score pour chaque parcours
            List<ParcoursRecommendationResponse> recommendations = new ArrayList<>();
            
            for (ParcoursApprentissage parcours : availableParcours) {
                try {
                    ParcoursRecommendationResponse recommendation = calculateRecommendationScore(parcours, userProfile);
                    if (recommendation.getScoreRecommendation() > 20.0) { // Seuil minimum
                        recommendations.add(recommendation);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors du calcul du score pour le parcours " + parcours.getTitre() + ": " + e.getMessage());
                    // Continuer avec les autres parcours
                }
            }
            
            // 4. Trier par score et limiter les résultats
            recommendations.sort((a, b) -> Double.compare(b.getScoreRecommendation(), a.getScoreRecommendation()));
            
            int limit = maxResults != null ? Math.min(maxResults, recommendations.size()) : Math.min(5, recommendations.size());
            List<ParcoursRecommendationResponse> topRecommendations = recommendations.subList(0, limit);
            
            System.out.println("✅ " + topRecommendations.size() + " recommandations générées avec succès");
            
            return topRecommendations;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors de la génération de recommandations pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Retourner une liste vide plutôt que de faire planter l'application
            return new ArrayList<>();
        }
    }

    /**
     * Génère des recommandations basées sur des critères spécifiques
     */
    public List<ParcoursRecommendationResponse> getRecommendationsByCriteria(User user, RecommendationRequest request) {
        try {
            System.out.println("🎯 Génération de recommandations par critères pour: " + user.getEmail());
            
            // Créer un profil temporaire basé sur les critères
            UserProfile customProfile = buildCustomProfile(user, request);
            
            // Récupérer les parcours disponibles
            List<ParcoursApprentissage> availableParcours = getAvailableParcours(user);
            
            // Filtrer selon les critères spécifiques
            availableParcours = filterParcoursByCriteria(availableParcours, request);
            
            // Calculer les scores
            List<ParcoursRecommendationResponse> recommendations = new ArrayList<>();
            
            for (ParcoursApprentissage parcours : availableParcours) {
                ParcoursRecommendationResponse recommendation = calculateRecommendationScore(parcours, customProfile);
                recommendations.add(recommendation);
            }
            
            // Trier et limiter
            recommendations.sort((a, b) -> Double.compare(b.getScoreRecommendation(), a.getScoreRecommendation()));
            
            int limit = request.getMaxRecommendations() != null ? 
                       Math.min(request.getMaxRecommendations(), recommendations.size()) : 
                       Math.min(10, recommendations.size());
            
            return recommendations.subList(0, limit);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération de recommandations par critères: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Construit le profil utilisateur pour l'analyse basé sur l'historique
     */
    private UserProfile buildUserProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        
        try {
            // Initialiser les listes vides (pas de préférences explicites)
            profile.setPreferredCategories(new ArrayList<>());
            profile.setLearningGoals(new ArrayList<>());
            profile.setInterests(new ArrayList<>());
            
            // Analyser l'historique d'apprentissage (source principale des recommandations)
            analyzeUserLearningHistory(profile);
            
            // Analyser l'historique des cours suivis
            analyzeUserCoursHistory(profile);
            
            // Récupérer les stats de gamification
            analyzeUserGamificationData(profile);
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la construction du profil utilisateur pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Profil minimal en cas d'erreur
            profile.setPreferredCategories(new ArrayList<>());
            profile.setLearningGoals(new ArrayList<>());
            profile.setInterests(new ArrayList<>());
            profile.setCompletedParcoursCount(0);
            profile.setAverageDifficultyLevel(1.0);
            profile.setAveragePerformance(0.0);
            profile.setCurrentLevel(1);
            profile.setTotalXP(0);
            profile.setAverageQuizScore(0.0);
            profile.setTotalQuizzesTaken(0);
            profile.setCompletedCoursCount(0);
            profile.setPreferredCategoriesFromCours(new ArrayList<>());
        }
        
        return profile;
    }

    /**
     * Analyse l'historique des cours suivis par l'utilisateur
     */
    private void analyzeUserCoursHistory(UserProfile profile) {
        User user = profile.getUser();
        
        try {
            // Récupérer tous les enrollments de l'utilisateur
            List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
            
            if (enrollments == null || enrollments.isEmpty()) {
                profile.setCompletedCoursCount(0);
                profile.setPreferredCategoriesFromCours(new ArrayList<>());
                return;
            }
            
            // Compter les cours avec progression significative (>50%)
            long completedCours = enrollments.stream()
                .filter(e -> e.getProgress() != null && e.getProgress() >= 50)
                .count();
            profile.setCompletedCoursCount((int) completedCours);
            
            // Analyser les catégories des cours suivis
            Map<String, Integer> categoryFrequency = new HashMap<>();
            Map<String, Double> categoryProgress = new HashMap<>();
            
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getCours() != null) {
                    String category = enrollment.getCours().getCategorie();
                    if (category != null && !category.trim().isEmpty()) {
                        categoryFrequency.put(category, categoryFrequency.getOrDefault(category, 0) + 1);
                        
                        // Pondérer par la progression
                        double progress = enrollment.getProgress() != null ? enrollment.getProgress() : 0;
                        categoryProgress.put(category, 
                            categoryProgress.getOrDefault(category, 0.0) + progress);
                    }
                }
            }
            
            // Trier par fréquence et progression combinées
            List<String> topCategories = categoryFrequency.entrySet().stream()
                .sorted((a, b) -> {
                    double scoreA = a.getValue() * (categoryProgress.getOrDefault(a.getKey(), 0.0) / 100.0 + 1);
                    double scoreB = b.getValue() * (categoryProgress.getOrDefault(b.getKey(), 0.0) / 100.0 + 1);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            profile.setPreferredCategoriesFromCours(topCategories);
            
            // Analyser le niveau de difficulté moyen des cours suivis
            double avgDifficulty = enrollments.stream()
                .filter(e -> e.getCours() != null && e.getCours().getNiveauDifficulte() != null)
                .mapToInt(e -> getDifficultyLevel(e.getCours().getNiveauDifficulte()))
                .average()
                .orElse(1.0);
            
            // Combiner avec le niveau des parcours si disponible
            if (profile.getAverageDifficultyLevel() != null && profile.getAverageDifficultyLevel() > 0) {
                profile.setAverageDifficultyLevel((profile.getAverageDifficultyLevel() + avgDifficulty) / 2);
            } else {
                profile.setAverageDifficultyLevel(avgDifficulty);
            }
            
            System.out.println("📊 Historique cours analysé: " + completedCours + " cours, catégories: " + topCategories);
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'analyse de l'historique des cours: " + e.getMessage());
            profile.setCompletedCoursCount(0);
            profile.setPreferredCategoriesFromCours(new ArrayList<>());
        }
    }

    /**
     * Analyse l'historique d'apprentissage de l'utilisateur
     */
    private void analyzeUserLearningHistory(UserProfile profile) {
        User user = profile.getUser();
        
        try {
            // Parcours complétés
            List<ParcoursInscription> completedParcours = inscriptionRepository.findByUserAndIsCompletedTrue(user);
            profile.setCompletedParcoursCount(completedParcours != null ? completedParcours.size() : 0);
            
            // Catégories les plus étudiées
            Map<String, Integer> categoryFrequency = new HashMap<>();
            if (completedParcours != null) {
                for (ParcoursInscription inscription : completedParcours) {
                    if (inscription.getParcours() != null) {
                        String category = inscription.getParcours().getCategorie();
                        if (category != null && !category.trim().isEmpty()) {
                            categoryFrequency.put(category, categoryFrequency.getOrDefault(category, 0) + 1);
                        }
                    }
                }
            }
            
            profile.setPreferredCategoriesFromHistory(
                categoryFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList())
            );
            
            // Niveau de difficulté moyen
            if (completedParcours != null && !completedParcours.isEmpty()) {
                double avgDifficulty = completedParcours.stream()
                    .filter(p -> p.getParcours() != null && p.getParcours().getNiveauDifficulte() != null)
                    .mapToInt(p -> getDifficultyLevel(p.getParcours().getNiveauDifficulte()))
                    .average()
                    .orElse(1.0);
                profile.setAverageDifficultyLevel(avgDifficulty);
            } else {
                profile.setAverageDifficultyLevel(1.0); // Débutant par défaut
            }
            
            // Performance moyenne
            List<ParcoursInscription> allInscriptions = inscriptionRepository.findByUser(user);
            if (allInscriptions != null && !allInscriptions.isEmpty()) {
                double avgProgression = allInscriptions.stream()
                    .filter(inscription -> inscription.getProgressionPourcentage() != null)
                    .mapToInt(ParcoursInscription::getProgressionPourcentage)
                    .average()
                    .orElse(0.0);
                profile.setAveragePerformance(avgProgression);
            } else {
                profile.setAveragePerformance(0.0);
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'analyse de l'historique d'apprentissage pour " + user.getEmail() + ": " + e.getMessage());
            
            // Valeurs par défaut en cas d'erreur
            profile.setCompletedParcoursCount(0);
            profile.setPreferredCategoriesFromHistory(new ArrayList<>());
            profile.setAverageDifficultyLevel(1.0);
            profile.setAveragePerformance(0.0);
        }
    }

    /**
     * Analyse les données de gamification
     */
    private void analyzeUserGamificationData(UserProfile profile) {
        User user = profile.getUser();
        
        try {
            // Récupérer les XP et niveau
            List<UserXP> userXPs = userXPRepository.findByUserId(user.getId());
            if (!userXPs.isEmpty()) {
                UserXP userXP = userXPs.get(0);
                profile.setCurrentLevel(userXP.getCurrentLevel() != null ? userXP.getCurrentLevel() : 1);
                profile.setTotalXP(userXP.getTotalXP() != null ? userXP.getTotalXP() : 0);
            } else {
                // Valeurs par défaut si pas de données XP
                profile.setCurrentLevel(1);
                profile.setTotalXP(0);
            }
            
            // Analyser les performances aux quiz avec gestion des valeurs nulles
            List<Object[]> quizStats = resultatQuizRepository.findUserQuizStatistics(user.getId());
            if (!quizStats.isEmpty()) {
                Object[] stats = quizStats.get(0);
                
                // Gestion sécurisée des valeurs nulles
                if (stats[0] != null) {
                    profile.setAverageQuizScore(((Number) stats[0]).doubleValue());
                } else {
                    profile.setAverageQuizScore(0.0);
                }
                
                if (stats[1] != null) {
                    profile.setTotalQuizzesTaken(((Number) stats[1]).intValue());
                } else {
                    profile.setTotalQuizzesTaken(0);
                }
            } else {
                // Valeurs par défaut si pas de quiz passés
                profile.setAverageQuizScore(0.0);
                profile.setTotalQuizzesTaken(0);
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'analyse des données de gamification pour " + user.getEmail() + ": " + e.getMessage());
            
            // Valeurs par défaut en cas d'erreur
            profile.setCurrentLevel(1);
            profile.setTotalXP(0);
            profile.setAverageQuizScore(0.0);
            profile.setTotalQuizzesTaken(0);
        }
    }

    /**
     * Calcule le score de recommandation pour un parcours
     */
    private ParcoursRecommendationResponse calculateRecommendationScore(ParcoursApprentissage parcours, UserProfile profile) {
        ParcoursRecommendationResponse response = new ParcoursRecommendationResponse();
        
        // Copier les données de base du parcours
        mapParcoursToResponse(parcours, response);
        
        // Calculer les scores individuels
        double scoreCategorie = calculateCategoryScore(parcours, profile);
        double scoreDifficulte = calculateDifficultyScore(parcours, profile);
        double scoreDuree = calculateDurationScore(parcours, profile);
        double scorePopularite = calculatePopularityScore(parcours);
        double scorePerformance = calculatePerformanceScore(parcours);
        double scorePrerequisMatch = calculatePrerequisiteScore(parcours, profile);
        
        // Pondération des scores
        double scoreTotal = 
            scoreCategorie * 0.25 +      // 25% - Catégorie/Intérêts
            scoreDifficulte * 0.20 +     // 20% - Niveau de difficulté
            scoreDuree * 0.15 +          // 15% - Durée appropriée
            scorePopularite * 0.15 +     // 15% - Popularité
            scorePerformance * 0.15 +    // 15% - Performance moyenne
            scorePrerequisMatch * 0.10;  // 10% - Prérequis

        // Bonus pour certificat si important pour l'utilisateur
        if (profile.getPreferences() != null && 
            Boolean.TRUE.equals(profile.getPreferences().getCertificationImportant()) && 
            Boolean.TRUE.equals(parcours.getCertificatEnabled())) {
            scoreTotal += 5.0; // Bonus de 5 points
        }
        
        // Sauvegarder les scores détaillés
        response.setScoreCategorie(scoreCategorie);
        response.setScoreDifficulte(scoreDifficulte);
        response.setScoreDuree(scoreDuree);
        response.setScorePopularite(scorePopularite);
        response.setScorePerformance(scorePerformance);
        response.setScorePrerequisMatch(scorePrerequisMatch);
        response.setScoreRecommendation(Math.min(100.0, scoreTotal));
        
        // Générer les raisons de recommandation
        response.setRaisonsRecommandation(generateRecommendationReasons(parcours, profile, response));
        
        // Déterminer le niveau de correspondance
        response.setNiveauCorrespondance(determineMatchLevel(scoreTotal));
        
        return response;
    }

    /**
     * Calcule le score basé sur la catégorie et les intérêts (basé sur l'historique)
     */
    private double calculateCategoryScore(ParcoursApprentissage parcours, UserProfile profile) {
        double score = 10.0; // Score de base
        
        String parcoursCategory = parcours.getCategorie();
        if (parcoursCategory == null) return score;
        
        // Score basé sur l'historique des parcours
        if (profile.getPreferredCategoriesFromHistory() != null && 
            profile.getPreferredCategoriesFromHistory().contains(parcoursCategory)) {
            score += 35.0;
        }
        
        // Score basé sur l'historique des cours
        if (profile.getPreferredCategoriesFromCours() != null && 
            profile.getPreferredCategoriesFromCours().contains(parcoursCategory)) {
            score += 30.0;
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Calcule le score basé sur le niveau de difficulté (basé sur l'historique)
     */
    private double calculateDifficultyScore(ParcoursApprentissage parcours, UserProfile profile) {
        if (parcours.getNiveauDifficulte() == null) return 25.0;
        
        int parcoursLevel = getDifficultyLevel(parcours.getNiveauDifficulte());
        double score = 25.0;
        
        // Ajustement basé sur l'historique d'apprentissage
        if (profile.getAverageDifficultyLevel() != null && profile.getAverageDifficultyLevel() > 0) {
            double historyLevel = profile.getAverageDifficultyLevel();
            double difference = Math.abs(parcoursLevel - historyLevel);
            
            // Recommander un niveau légèrement supérieur pour la progression
            if (parcoursLevel == Math.ceil(historyLevel) || parcoursLevel == Math.floor(historyLevel)) {
                score = 45.0; // Niveau actuel ou juste au-dessus
            } else if (difference <= 0.5) {
                score = 50.0; // Très proche
            } else if (difference <= 1.0) {
                score = 40.0; // Proche
            } else if (difference <= 1.5) {
                score = 30.0; // Acceptable
            } else {
                score = 15.0; // Trop éloigné
            }
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Calcule le score basé sur la durée (basé sur des critères généraux)
     */
    private double calculateDurationScore(ParcoursApprentissage parcours, UserProfile profile) {
        if (parcours.getDureeEstimeeHeures() == null) return 30.0;
        
        int parcoursDuration = parcours.getDureeEstimeeHeures();
        double score = 30.0;
        
        // Favoriser les parcours de durée raisonnable (5-30h)
        if (parcoursDuration >= 5 && parcoursDuration <= 30) {
            score = 45.0;
        } else if (parcoursDuration < 5) {
            score = 35.0; // Courts mais acceptables
        } else if (parcoursDuration <= 50) {
            score = 30.0; // Longs mais faisables
        } else {
            score = 20.0; // Très longs
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Calcule le score de popularité
     */
    private double calculatePopularityScore(ParcoursApprentissage parcours) {
        long totalInscriptions = inscriptionRepository.countByParcours(parcours);
        long completedInscriptions = inscriptionRepository.countByParcoursAndIsCompletedTrue(parcours);
        
        // Score basé sur le nombre d'inscriptions (logarithmique)
        double popularityScore = Math.min(25.0, Math.log(totalInscriptions + 1) * 5);
        
        // Bonus pour taux de completion élevé
        if (totalInscriptions > 0) {
            double completionRate = (double) completedInscriptions / totalInscriptions;
            popularityScore += completionRate * 25.0;
        }
        
        return Math.min(50.0, popularityScore);
    }

    /**
     * Calcule le score de performance moyenne
     */
    private double calculatePerformanceScore(ParcoursApprentissage parcours) {
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByParcours(parcours);
        
        if (inscriptions.isEmpty()) return 25.0; // Score neutre
        
        double averageProgression = inscriptions.stream()
            .mapToInt(ParcoursInscription::getProgressionPourcentage)
            .average()
            .orElse(0.0);
        
        // Convertir la progression moyenne en score (0-50)
        return (averageProgression / 100.0) * 50.0;
    }

    /**
     * Calcule le score de correspondance des prérequis
     */
    private double calculatePrerequisiteScore(ParcoursApprentissage parcours, UserProfile profile) {
        String prerequis = parcours.getPrerequis();
        if (prerequis == null || prerequis.trim().isEmpty()) {
            return 50.0; // Pas de prérequis = parfait pour tous
        }
        
        // Analyser si l'utilisateur a les compétences requises
        // Basé sur les parcours complétés et le niveau
        double score = 25.0; // Score de base
        
        // Bonus si l'utilisateur a un niveau élevé
        if (profile.getCurrentLevel() != null && profile.getCurrentLevel() >= 3) {
            score += 15.0;
        }
        
        // Bonus si l'utilisateur a complété des parcours similaires
        if (profile.getCompletedParcoursCount() != null && profile.getCompletedParcoursCount() > 0) {
            score += 10.0;
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Génère les raisons de recommandation basées sur l'historique
     */
    private List<String> generateRecommendationReasons(ParcoursApprentissage parcours, UserProfile profile, ParcoursRecommendationResponse response) {
        List<String> reasons = new ArrayList<>();
        
        // Raisons basées sur l'historique des parcours
        if (profile.getPreferredCategoriesFromHistory() != null && 
            profile.getPreferredCategoriesFromHistory().contains(parcours.getCategorie())) {
            reasons.add("Similaire à vos parcours précédents");
        }
        
        // Raisons basées sur l'historique des cours
        if (profile.getPreferredCategoriesFromCours() != null && 
            profile.getPreferredCategoriesFromCours().contains(parcours.getCategorie())) {
            reasons.add("Dans la continuité de vos cours suivis");
        }
        
        if (response.getScoreDifficulte() >= 40.0) {
            reasons.add("Niveau adapté à votre progression");
        }
        
        if (response.getScorePopularite() >= 35.0) {
            reasons.add("Parcours populaire avec un bon taux de réussite");
        }
        
        if (response.getScorePerformance() >= 40.0) {
            reasons.add("Excellentes performances des autres apprenants");
        }
        
        // Raisons spécifiques
        if (Boolean.TRUE.equals(parcours.getCertificatEnabled())) {
            reasons.add("Certificat disponible à la fin");
        }
        
        if (parcours.getPointsBonus() != null && parcours.getPointsBonus() > 100) {
            reasons.add("Récompenses XP importantes (" + parcours.getPointsBonus() + " points)");
        }
        
        return reasons.isEmpty() ? Arrays.asList("Recommandé pour élargir vos compétences") : reasons;
    }

    /**
     * Détermine le niveau de correspondance
     */
    private String determineMatchLevel(double score) {
        if (score >= 80.0) return "PARFAIT";
        else if (score >= 60.0) return "BON";
        else if (score >= 40.0) return "ACCEPTABLE";
        else return "FAIBLE";
    }

    // Méthodes utilitaires
    
    private List<ParcoursApprentissage> getAvailableParcours(User user) {
        List<ParcoursApprentissage> allPublished = parcoursRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
        
        // Exclure les parcours déjà inscrits
        Set<Long> inscritParcoursIds = inscriptionRepository.findByUser(user).stream()
            .map(inscription -> inscription.getParcours().getId())
            .collect(Collectors.toSet());
        
        return allPublished.stream()
            .filter(parcours -> !inscritParcoursIds.contains(parcours.getId()))
            .collect(Collectors.toList());
    }
    
    private List<ParcoursApprentissage> filterParcoursByCriteria(List<ParcoursApprentissage> parcours, RecommendationRequest request) {
        return parcours.stream()
            .filter(p -> {
                // Filtrer par catégorie
                if (request.getPreferredCategories() != null && !request.getPreferredCategories().isEmpty()) {
                    if (p.getCategorie() == null || !request.getPreferredCategories().contains(p.getCategorie())) {
                        return false;
                    }
                }
                
                // Filtrer par difficulté
                if (request.getPreferredDifficulty() != null) {
                    if (p.getNiveauDifficulte() != request.getPreferredDifficulty()) {
                        return false;
                    }
                }
                
                // Filtrer par durée
                if (request.getPreferredDurationMin() != null && p.getDureeEstimeeHeures() != null) {
                    if (p.getDureeEstimeeHeures() < request.getPreferredDurationMin()) {
                        return false;
                    }
                }
                
                if (request.getPreferredDurationMax() != null && p.getDureeEstimeeHeures() != null) {
                    if (p.getDureeEstimeeHeures() > request.getPreferredDurationMax()) {
                        return false;
                    }
                }
                
                // Filtrer par certificat
                if (Boolean.TRUE.equals(request.getCertificationImportant())) {
                    if (!Boolean.TRUE.equals(p.getCertificatEnabled())) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    private UserProfile buildCustomProfile(User user, RecommendationRequest request) {
        UserProfile profile = buildUserProfile(user); // Commencer avec le profil existant
        
        // Remplacer par les critères personnalisés
        if (request.getPreferredCategories() != null) {
            profile.setPreferredCategories(request.getPreferredCategories());
        }
        
        // Créer des préférences temporaires
        UserPreferences tempPrefs = new UserPreferences();
        tempPrefs.setPreferredDifficulty(request.getPreferredDifficulty());
        tempPrefs.setLearningStyle(request.getLearningStyle());
        tempPrefs.setTimeAvailabilityHours(request.getTimeAvailabilityHours());
        tempPrefs.setCareerFocus(request.getCareerFocus());
        tempPrefs.setPreferredDurationMin(request.getPreferredDurationMin());
        tempPrefs.setPreferredDurationMax(request.getPreferredDurationMax());
        tempPrefs.setChallengePreference(request.getChallengePreference());
        tempPrefs.setCertificationImportant(request.getCertificationImportant());
        
        profile.setPreferences(tempPrefs);
        
        if (request.getLearningGoals() != null) {
            profile.setLearningGoals(request.getLearningGoals());
        }
        
        if (request.getInterests() != null) {
            profile.setInterests(request.getInterests());
        }
        
        return profile;
    }
    
    private void mapParcoursToResponse(ParcoursApprentissage parcours, ParcoursRecommendationResponse response) {
        response.setId(parcours.getId());
        response.setTitre(parcours.getTitre());
        response.setDescription(parcours.getDescription());
        response.setThumbnailUrl(parcours.getThumbnailUrl());
        response.setCategorie(parcours.getCategorie());
        response.setNiveauDifficulte(parcours.getNiveauDifficulte());
        response.setDureeEstimeeHeures(parcours.getDureeEstimeeHeures());
        response.setPrerequis(parcours.getPrerequis());
        response.setTypeParcours(parcours.getTypeParcours());
        response.setPointsBonus(parcours.getPointsBonus());
        response.setCertificatEnabled(parcours.getCertificatEnabled());
        response.setFormateurNom(parcours.getFormateur().getFirstName() + " " + parcours.getFormateur().getLastName());
        
        // Statistiques
        response.setNombreInscriptions((int) inscriptionRepository.countByParcours(parcours));
        
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByParcours(parcours);
        if (!inscriptions.isEmpty()) {
            double avgProgression = inscriptions.stream()
                .mapToInt(ParcoursInscription::getProgressionPourcentage)
                .average()
                .orElse(0.0);
            response.setProgressionMoyenne(avgProgression);
        }
        
        response.setIsInscrit(false); // Sera mis à jour si nécessaire
        response.setProgressionUtilisateur(0);
    }
    
    private List<String> parseJsonArray(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Nettoyer la chaîne JSON
            String cleanJson = jsonString.trim();
            
            // Vérifier si c'est un JSON valide
            if (!cleanJson.startsWith("[") || !cleanJson.endsWith("]")) {
                System.err.println("⚠️ Format JSON invalide: " + cleanJson);
                return new ArrayList<>();
            }
            
            List<String> result = objectMapper.readValue(cleanJson, new TypeReference<List<String>>() {});
            return result != null ? result : new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing JSON: " + jsonString + " - " + e.getMessage());
            
            // Tentative de parsing manuel simple pour les cas basiques
            try {
                if (jsonString.contains(",")) {
                    String content = jsonString.replace("[", "").replace("]", "").replace("\"", "");
                    return Arrays.stream(content.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            } catch (Exception e2) {
                System.err.println("⚠️ Échec du parsing manuel: " + e2.getMessage());
            }
            
            return new ArrayList<>();
        }
    }
    
    private int getDifficultyLevel(NiveauDifficulte niveau) {
        switch (niveau) {
            case DEBUTANT: return 1;
            case INTERMEDIAIRE: return 2;
            case AVANCE: return 3;
            case EXPERT: return 4;
            default: return 1;
        }
    }

    // ==================== RECOMMANDATIONS DE COURS ====================

    /**
     * Génère des recommandations de cours personnalisées pour un utilisateur
     */
    public List<CoursRecommendationResponse> getPersonalizedCoursRecommendations(User user, Integer maxResults) {
        try {
            System.out.println("🤖 Génération de recommandations de cours IA pour: " + user.getEmail());
            
            // 1. Récupérer le profil utilisateur
            UserProfile userProfile = buildUserProfile(user);
            
            // 2. Récupérer tous les cours disponibles non inscrits
            List<Cours> availableCours = getAvailableCours(user);
            
            if (availableCours.isEmpty()) {
                System.out.println("❌ Aucun cours disponible pour recommandation");
                return new ArrayList<>();
            }
            
            System.out.println("📚 " + availableCours.size() + " cours disponibles pour analyse");
            
            // 3. Calculer le score pour chaque cours
            List<CoursRecommendationResponse> recommendations = new ArrayList<>();
            
            for (Cours cours : availableCours) {
                try {
                    CoursRecommendationResponse recommendation = calculateCoursRecommendationScore(cours, userProfile);
                    if (recommendation.getScoreRecommendation() > 15.0) {
                        recommendations.add(recommendation);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors du calcul du score pour le cours " + cours.getTitre() + ": " + e.getMessage());
                }
            }
            
            // 4. Trier par score et limiter les résultats
            recommendations.sort((a, b) -> Double.compare(b.getScoreRecommendation(), a.getScoreRecommendation()));
            
            int limit = maxResults != null ? Math.min(maxResults, recommendations.size()) : Math.min(8, recommendations.size());
            List<CoursRecommendationResponse> topRecommendations = recommendations.subList(0, Math.min(limit, recommendations.size()));
            
            System.out.println("✅ " + topRecommendations.size() + " recommandations de cours générées avec succès");
            
            return topRecommendations;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors de la génération de recommandations de cours: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Génère des recommandations de cours basées sur des critères spécifiques
     */
    public List<CoursRecommendationResponse> getCoursRecommendationsByCriteria(User user, RecommendationRequest request) {
        try {
            System.out.println("🎯 Génération de recommandations de cours par critères pour: " + user.getEmail());
            
            UserProfile customProfile = buildCustomProfile(user, request);
            List<Cours> availableCours = getAvailableCours(user);
            
            // Filtrer selon les critères
            availableCours = filterCoursByCriteria(availableCours, request);
            
            List<CoursRecommendationResponse> recommendations = new ArrayList<>();
            
            for (Cours cours : availableCours) {
                CoursRecommendationResponse recommendation = calculateCoursRecommendationScore(cours, customProfile);
                recommendations.add(recommendation);
            }
            
            recommendations.sort((a, b) -> Double.compare(b.getScoreRecommendation(), a.getScoreRecommendation()));
            
            int limit = request.getMaxRecommendations() != null ? 
                       Math.min(request.getMaxRecommendations(), recommendations.size()) : 
                       Math.min(10, recommendations.size());
            
            return recommendations.subList(0, Math.min(limit, recommendations.size()));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération de recommandations de cours par critères: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les cours disponibles pour un utilisateur
     */
    private List<Cours> getAvailableCours(User user) {
        List<Cours> allCours = coursRepository.findByArchivedFalse();
        
        // Exclure les cours déjà inscrits
        Set<Long> enrolledCoursIds = enrollmentRepository.findByUser(user).stream()
            .map(enrollment -> enrollment.getCours().getId())
            .collect(Collectors.toSet());
        
        return allCours.stream()
            .filter(cours -> !enrolledCoursIds.contains(cours.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Filtre les cours selon les critères
     */
    private List<Cours> filterCoursByCriteria(List<Cours> cours, RecommendationRequest request) {
        return cours.stream()
            .filter(c -> {
                // Filtrer par catégorie
                if (request.getPreferredCategories() != null && !request.getPreferredCategories().isEmpty()) {
                    if (c.getCategorie() == null || !request.getPreferredCategories().contains(c.getCategorie())) {
                        return false;
                    }
                }
                
                // Filtrer par difficulté
                if (request.getPreferredDifficulty() != null) {
                    if (c.getNiveauDifficulte() != request.getPreferredDifficulty()) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * Calcule le score de recommandation pour un cours
     */
    private CoursRecommendationResponse calculateCoursRecommendationScore(Cours cours, UserProfile profile) {
        CoursRecommendationResponse response = new CoursRecommendationResponse();
        
        // Mapper les données du cours
        mapCoursToResponse(cours, response);
        
        // Calculer les scores individuels
        double scoreCategorie = calculateCoursCategoryScore(cours, profile);
        double scoreDifficulte = calculateCoursDifficultyScore(cours, profile);
        double scorePopularite = calculateCoursPopularityScore(cours);
        double scoreKeywords = calculateCoursKeywordsScore(cours, profile);
        
        // Pondération des scores
        double scoreTotal = 
            scoreCategorie * 0.30 +      // 30% - Catégorie/Intérêts
            scoreDifficulte * 0.25 +     // 25% - Niveau de difficulté
            scorePopularite * 0.20 +     // 20% - Popularité
            scoreKeywords * 0.25;        // 25% - Mots-clés/Intérêts
        
        // Sauvegarder les scores détaillés
        response.setScoreCategorie(scoreCategorie);
        response.setScoreDifficulte(scoreDifficulte);
        response.setScorePopularite(scorePopularite);
        response.setScoreKeywords(scoreKeywords);
        response.setScoreRecommendation(Math.min(100.0, scoreTotal));
        
        // Générer les raisons de recommandation
        response.setRaisonsRecommandation(generateCoursRecommendationReasons(cours, profile, response));
        
        // Déterminer le niveau de correspondance
        response.setNiveauCorrespondance(determineMatchLevel(scoreTotal));
        
        return response;
    }

    /**
     * Calcule le score basé sur la catégorie du cours (basé sur l'historique)
     */
    private double calculateCoursCategoryScore(Cours cours, UserProfile profile) {
        double score = 10.0; // Score de base
        
        String coursCategory = cours.getCategorie();
        if (coursCategory == null) return score;
        
        // Score basé sur l'historique des parcours
        if (profile.getPreferredCategoriesFromHistory() != null && 
            profile.getPreferredCategoriesFromHistory().contains(coursCategory)) {
            score += 30.0;
        }
        
        // Score basé sur l'historique des cours
        if (profile.getPreferredCategoriesFromCours() != null && 
            profile.getPreferredCategoriesFromCours().contains(coursCategory)) {
            score += 35.0;
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Calcule le score basé sur le niveau de difficulté du cours (basé sur l'historique)
     */
    private double calculateCoursDifficultyScore(Cours cours, UserProfile profile) {
        if (cours.getNiveauDifficulte() == null) return 25.0;
        
        int coursLevel = getDifficultyLevel(cours.getNiveauDifficulte());
        double score = 25.0;
        
        // Ajustement basé sur l'historique d'apprentissage
        if (profile.getAverageDifficultyLevel() != null && profile.getAverageDifficultyLevel() > 0) {
            double historyLevel = profile.getAverageDifficultyLevel();
            double difference = Math.abs(coursLevel - historyLevel);
            
            // Recommander un niveau légèrement supérieur pour la progression
            if (coursLevel == Math.ceil(historyLevel) || coursLevel == Math.floor(historyLevel)) {
                score = 45.0;
            } else if (difference <= 0.5) {
                score = 50.0;
            } else if (difference <= 1.0) {
                score = 40.0;
            } else if (difference <= 1.5) {
                score = 30.0;
            } else {
                score = 15.0;
            }
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Calcule le score de popularité du cours
     */
    private double calculateCoursPopularityScore(Cours cours) {
        long totalEnrollments = enrollmentRepository.countByCours(cours);
        
        // Score basé sur le nombre d'inscriptions (logarithmique)
        double popularityScore = Math.min(30.0, Math.log(totalEnrollments + 1) * 8);
        
        // Bonus pour les cours récents avec des inscriptions
        if (cours.getCreatedAt() != null) {
            long ageInDays = (System.currentTimeMillis() - cours.getCreatedAt()) / (1000 * 60 * 60 * 24);
            if (ageInDays < 30 && totalEnrollments > 5) {
                popularityScore += 15.0; // Bonus pour cours récent populaire
            }
        }
        
        return Math.min(50.0, popularityScore);
    }

    /**
     * Calcule le score basé sur les mots-clés du cours (basé sur l'historique)
     */
    private double calculateCoursKeywordsScore(Cours cours, UserProfile profile) {
        double score = 20.0; // Score de base
        
        String keywords = cours.getKeywords();
        if (keywords == null || keywords.trim().isEmpty()) return score;
        
        // Bonus si le cours a des mots-clés (contenu bien documenté)
        score += 10.0;
        
        // Bonus supplémentaire si les mots-clés correspondent aux catégories de l'historique
        String[] keywordArray = keywords.toLowerCase().split(",");
        
        if (profile.getPreferredCategoriesFromCours() != null) {
            for (String category : profile.getPreferredCategoriesFromCours()) {
                for (String keyword : keywordArray) {
                    if (keyword.trim().contains(category.toLowerCase()) ||
                        category.toLowerCase().contains(keyword.trim())) {
                        score += 15.0;
                        break;
                    }
                }
            }
        }
        
        return Math.min(50.0, score);
    }

    /**
     * Génère les raisons de recommandation pour un cours (basé sur l'historique)
     */
    private List<String> generateCoursRecommendationReasons(Cours cours, UserProfile profile, CoursRecommendationResponse response) {
        List<String> reasons = new ArrayList<>();
        
        // Raisons basées sur l'historique des parcours
        if (profile.getPreferredCategoriesFromHistory() != null && 
            profile.getPreferredCategoriesFromHistory().contains(cours.getCategorie())) {
            reasons.add("Similaire à vos parcours précédents");
        }
        
        // Raisons basées sur l'historique des cours
        if (profile.getPreferredCategoriesFromCours() != null && 
            profile.getPreferredCategoriesFromCours().contains(cours.getCategorie())) {
            reasons.add("Dans la continuité de vos cours suivis");
        }
        
        if (response.getScoreDifficulte() >= 40.0) {
            reasons.add("Niveau adapté à votre progression");
        }
        
        if (response.getScorePopularite() >= 30.0) {
            reasons.add("Cours populaire auprès des apprenants");
        }
        
        // Raison par défaut
        if (reasons.isEmpty()) {
            reasons.add("Recommandé pour élargir vos compétences");
        }
        
        return reasons;
    }

    /**
     * Mappe un cours vers la réponse de recommandation
     */
    private void mapCoursToResponse(Cours cours, CoursRecommendationResponse response) {
        response.setId(cours.getId());
        response.setTitre(cours.getTitre());
        response.setDescription(cours.getDescription());
        response.setThumbnailUrl(cours.getThumbnailUrl());
        response.setCategorie(cours.getCategorie());
        response.setNiveauDifficulte(cours.getNiveauDifficulte());
        response.setKeywords(cours.getKeywords());
        response.setFormateurNom(cours.getFormateur().getFirstName() + " " + cours.getFormateur().getLastName());
        response.setIsEnrolled(false);
        response.setProgressionUtilisateur(0);
    }

    /**
     * Classe interne pour le profil utilisateur basé sur l'historique
     */
    private static class UserProfile {
        private User user;
        private UserPreferences preferences;
        private List<String> preferredCategories;
        private List<String> preferredCategoriesFromHistory;
        private List<String> preferredCategoriesFromCours;
        private List<String> learningGoals;
        private List<String> interests;
        private Integer completedParcoursCount;
        private Integer completedCoursCount;
        private Double averageDifficultyLevel;
        private Double averagePerformance;
        private Integer currentLevel;
        private Integer totalXP;
        private Double averageQuizScore;
        private Integer totalQuizzesTaken;
        
        // Getters et Setters
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        
        public UserPreferences getPreferences() { return preferences; }
        public void setPreferences(UserPreferences preferences) { this.preferences = preferences; }
        
        public List<String> getPreferredCategories() { return preferredCategories; }
        public void setPreferredCategories(List<String> preferredCategories) { this.preferredCategories = preferredCategories; }
        
        public List<String> getPreferredCategoriesFromHistory() { return preferredCategoriesFromHistory; }
        public void setPreferredCategoriesFromHistory(List<String> preferredCategoriesFromHistory) { this.preferredCategoriesFromHistory = preferredCategoriesFromHistory; }
        
        public List<String> getPreferredCategoriesFromCours() { return preferredCategoriesFromCours; }
        public void setPreferredCategoriesFromCours(List<String> preferredCategoriesFromCours) { this.preferredCategoriesFromCours = preferredCategoriesFromCours; }
        
        public List<String> getLearningGoals() { return learningGoals; }
        public void setLearningGoals(List<String> learningGoals) { this.learningGoals = learningGoals; }
        
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
        
        public Integer getCompletedParcoursCount() { return completedParcoursCount; }
        public void setCompletedParcoursCount(Integer completedParcoursCount) { this.completedParcoursCount = completedParcoursCount; }
        
        public Integer getCompletedCoursCount() { return completedCoursCount; }
        public void setCompletedCoursCount(Integer completedCoursCount) { this.completedCoursCount = completedCoursCount; }
        
        public Double getAverageDifficultyLevel() { return averageDifficultyLevel; }
        public void setAverageDifficultyLevel(Double averageDifficultyLevel) { this.averageDifficultyLevel = averageDifficultyLevel; }
        
        public Double getAveragePerformance() { return averagePerformance; }
        public void setAveragePerformance(Double averagePerformance) { this.averagePerformance = averagePerformance; }
        
        public Integer getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }
        
        public Integer getTotalXP() { return totalXP; }
        public void setTotalXP(Integer totalXP) { this.totalXP = totalXP; }
        
        public Double getAverageQuizScore() { return averageQuizScore; }
        public void setAverageQuizScore(Double averageQuizScore) { this.averageQuizScore = averageQuizScore; }
        
        public Integer getTotalQuizzesTaken() { return totalQuizzesTaken; }
        public void setTotalQuizzesTaken(Integer totalQuizzesTaken) { this.totalQuizzesTaken = totalQuizzesTaken; }
    }
}