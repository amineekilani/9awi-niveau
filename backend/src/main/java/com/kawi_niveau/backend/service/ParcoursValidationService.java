package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParcoursValidationService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcoursEtapeRepository parcoursEtapeRepository;

    /**
     * Valide si une étape est débloquée pour un utilisateur
     */
    public boolean isEtapeDebloquee(ParcoursEtape etape, User user, List<ParcoursEtape> etapesPrecedentes) {
        System.out.println("🔍 DÉBLOQUAGE ÉTAPE " + etape.getOrdreEtape() + " pour " + user.getEmail());
        System.out.println("   - Cours: " + etape.getCours().getTitre());
        System.out.println("   - Type parcours: " + etape.getParcours().getTypeParcours());
        
        // Si pas d'inscription au cours, étape verrouillée
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
        if (enrollment.isEmpty()) {
            System.out.println("   - ❌ PAS D'INSCRIPTION AU COURS - ÉTAPE VERROUILLÉE");
            return false;
        }
        System.out.println("   - ✅ Inscrit au cours");

        // Pour un parcours flexible, toutes les étapes sont débloquées
        if (etape.getParcours().getTypeParcours() == TypeParcours.FLEXIBLE) {
            System.out.println("   - ✅ PARCOURS FLEXIBLE - ÉTAPE DÉBLOQUÉE");
            return true;
        }

        // Pour un parcours linéaire, vérifier l'ordre et les prérequis
        if (etape.getOrdreEtape() == 1) {
            System.out.println("   - ✅ PREMIÈRE ÉTAPE - DÉBLOQUÉE");
            return true; // La première étape est toujours débloquée
        }

        System.out.println("   - 🔍 PARCOURS LINÉAIRE - Vérification étapes précédentes");
        // Vérifier que toutes les étapes précédentes sont complètes
        for (ParcoursEtape etapePrecedente : etapesPrecedentes) {
            if (etapePrecedente.getOrdreEtape() < etape.getOrdreEtape()) {
                boolean precedenteComplete = isEtapeComplete(etapePrecedente, user);
                System.out.println("     - Étape " + etapePrecedente.getOrdreEtape() + ": " + (precedenteComplete ? "COMPLÈTE" : "INCOMPLÈTE"));
                if (!precedenteComplete) {
                    System.out.println("   - ❌ ÉTAPE PRÉCÉDENTE INCOMPLÈTE - ÉTAPE VERROUILLÉE");
                    return false;
                }
            }
        }

        System.out.println("   - ✅ TOUTES LES ÉTAPES PRÉCÉDENTES COMPLÈTES - ÉTAPE DÉBLOQUÉE");
        return true;
    }

    /**
     * Valide si une étape est complète pour un utilisateur
     */
    public boolean isEtapeComplete(ParcoursEtape etape, User user) {
        System.out.println("🔍 VALIDATION ÉTAPE " + etape.getOrdreEtape() + " pour " + user.getEmail());
        
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
        if (enrollment.isEmpty()) {
            System.out.println("❌ Pas d'inscription au cours " + etape.getCours().getTitre());
            return false;
        }

        Enrollment enroll = enrollment.get();
        System.out.println("📈 Progression cours: " + enroll.getProgress() + "%");
        
        // Vérifier le pourcentage de completion requis (gérer les valeurs NULL)
        Integer completionRequis = etape.getPourcentageCompletionRequis();
        System.out.println("📋 Completion requise: " + (completionRequis != null ? completionRequis + "%" : "NULL"));
        
        if (completionRequis != null && completionRequis > 0) {
            float progressionRequise = completionRequis.floatValue();
            if (enroll.getProgress() < progressionRequise) {
                System.out.println("❌ Progression insuffisante: " + enroll.getProgress() + "% < " + progressionRequise + "%");
                return false;
            }
            System.out.println("✅ Progression suffisante: " + enroll.getProgress() + "% >= " + progressionRequise + "%");
        } else {
            // Si pas de condition spécifique, on considère que 100% est requis par défaut
            if (enroll.getProgress() < 100.0f) {
                System.out.println("❌ Progression insuffisante (défaut): " + enroll.getProgress() + "% < 100%");
                return false;
            }
            System.out.println("✅ Progression suffisante (défaut): " + enroll.getProgress() + "% >= 100%");
        }

        // Vérifier les quiz obligatoires ET le score minimum (logique combinée)
        Boolean quizObligatoires = etape.getQuizObligatoires();
        Integer scoreMinimum = etape.getScoreMinimum();
        System.out.println("📋 Quiz obligatoires: " + (quizObligatoires != null ? quizObligatoires : "NULL"));
        System.out.println("📋 Score minimum: " + (scoreMinimum != null ? scoreMinimum + "%" : "NULL"));
        
        if (quizObligatoires != null && quizObligatoires) {
            // Quiz OBLIGATOIRES - Vérifier score ET réussite
            System.out.println("🎯 Quiz obligatoires activés");
            
            // Vérifier le score minimum si défini
            if (scoreMinimum != null && scoreMinimum > 0) {
                double scoreObtenu = getMeilleurScoreQuizCours(user, etape.getCours());
                System.out.println("📊 Score obtenu: " + scoreObtenu + "%");
                if (scoreObtenu < scoreMinimum) {
                    System.out.println("❌ Score insuffisant pour quiz obligatoires: " + scoreObtenu + "% < " + scoreMinimum + "%");
                    return false;
                }
                System.out.println("✅ Score suffisant pour quiz obligatoires: " + scoreObtenu + "% >= " + scoreMinimum + "%");
            }
            
            // Si l'étape a un score minimum défini, utiliser ce score comme seuil
            // Sinon, utiliser 60% par défaut
            double seuilQuiz = (scoreMinimum != null && scoreMinimum > 0) ? scoreMinimum : 60.0;
            System.out.println("📊 Seuil quiz utilisé: " + seuilQuiz + "%");
            
            if (!hasPassedRequiredQuizzesWithThreshold(user, etape.getCours(), seuilQuiz)) {
                System.out.println("❌ Quiz obligatoires non réussis avec seuil " + seuilQuiz + "%");
                return false;
            }
            System.out.println("✅ Quiz obligatoires réussis avec seuil " + seuilQuiz + "%");
        } else {
            // Quiz NON OBLIGATOIRES - Ignorer le score minimum
            System.out.println("ℹ️ Quiz non obligatoires - Score minimum ignoré");
            if (scoreMinimum != null && scoreMinimum > 0) {
                System.out.println("⚠️ Score minimum (" + scoreMinimum + "%) défini mais quiz non obligatoires - IGNORÉ");
            }
        }

        System.out.println("🎉 ÉTAPE " + etape.getOrdreEtape() + " VALIDÉE pour " + user.getEmail());
        return true;
    }

    /**
     * Récupère la progression d'un cours pour un utilisateur
     */
    public float getProgressionCours(User user, Cours cours) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, cours);
        return enrollment.map(Enrollment::getProgress).orElse(0.0f);
    }

    /**
     * Récupère le meilleur score obtenu dans les quiz d'un cours
     */
    public double getMeilleurScoreQuizCours(User user, Cours cours) {
        List<Quiz> quizzes = quizRepository.findByCours(cours);
        double meilleurScore = 0.0;

        for (Quiz quiz : quizzes) {
            Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
            if (meilleurResultat.isPresent()) {
                meilleurScore = Math.max(meilleurScore, meilleurResultat.get().getScore());
            }
        }

        return meilleurScore;
    }

    /**
     * Vérifie si l'utilisateur a réussi tous les quiz obligatoires d'un cours
     */
    public boolean hasPassedRequiredQuizzes(User user, Cours cours) {
        List<Quiz> quizzes = quizRepository.findByCours(cours);
        System.out.println("🔍 VÉRIFICATION QUIZ OBLIGATOIRES pour cours: " + cours.getTitre());
        System.out.println("📊 Nombre de quiz dans le cours: " + quizzes.size());
        
        if (quizzes.isEmpty()) {
            System.out.println("ℹ️ Aucun quiz dans le cours - Quiz obligatoires considérés comme réussis");
            return true;
        }
        
        for (Quiz quiz : quizzes) {
            System.out.println("🔍 Vérification quiz: " + quiz.getTitre() + " (ID: " + quiz.getId() + ")");
            
            Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
            
            if (meilleurResultat.isEmpty()) {
                System.out.println("❌ Aucun résultat trouvé pour le quiz: " + quiz.getTitre());
                return false;
            }
            
            double score = meilleurResultat.get().getScore();
            System.out.println("📊 Score obtenu: " + score + "% pour quiz: " + quiz.getTitre());
            
            // Considérer qu'un quiz est réussi avec un score >= 60%
            if (score < 60.0) {
                System.out.println("❌ Score insuffisant: " + score + "% < 60% pour quiz: " + quiz.getTitre());
                return false;
            }
            
            System.out.println("✅ Quiz réussi: " + quiz.getTitre() + " avec " + score + "%");
        }

        System.out.println("🎉 TOUS LES QUIZ OBLIGATOIRES RÉUSSIS pour cours: " + cours.getTitre());
        return true;
    }

    /**
     * Vérifie si l'utilisateur a réussi tous les quiz obligatoires d'un cours avec un seuil personnalisé
     */
    public boolean hasPassedRequiredQuizzesWithThreshold(User user, Cours cours, double seuil) {
        List<Quiz> quizzes = quizRepository.findByCours(cours);
        System.out.println("🔍 VÉRIFICATION QUIZ OBLIGATOIRES avec seuil " + seuil + "% pour cours: " + cours.getTitre());
        System.out.println("📊 Nombre de quiz dans le cours: " + quizzes.size());
        
        if (quizzes.isEmpty()) {
            System.out.println("ℹ️ Aucun quiz dans le cours - Quiz obligatoires considérés comme réussis");
            return true;
        }
        
        for (Quiz quiz : quizzes) {
            System.out.println("🔍 Vérification quiz: " + quiz.getTitre() + " (ID: " + quiz.getId() + ")");
            
            Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
            
            if (meilleurResultat.isEmpty()) {
                System.out.println("❌ Aucun résultat trouvé pour le quiz: " + quiz.getTitre());
                return false;
            }
            
            double score = meilleurResultat.get().getScore();
            System.out.println("📊 Score obtenu: " + score + "% pour quiz: " + quiz.getTitre());
            
            if (score < seuil) {
                System.out.println("❌ Score insuffisant: " + score + "% < " + seuil + "% pour quiz: " + quiz.getTitre());
                return false;
            }
            
            System.out.println("✅ Quiz réussi: " + quiz.getTitre() + " avec " + score + "% (>= " + seuil + "%)");
        }

        System.out.println("🎉 TOUS LES QUIZ OBLIGATOIRES RÉUSSIS avec seuil " + seuil + "% pour cours: " + cours.getTitre());
        return true;
    }

    /**
     * Vérifie les conditions spécifiques d'une étape
     */
    public EtapeValidationResult validateEtapeConditions(ParcoursEtape etape, User user) {
        EtapeValidationResult result = new EtapeValidationResult();
        
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
        if (enrollment.isEmpty()) {
            result.setProgressionCours(0.0f);
            result.setScoreObtenu(0.0);
            result.setScoreMinimumRempli(false);
            result.setPourcentageCompletionRempli(false);
            result.setQuizObligatoiresRemplis(false);
            return result;
        }

        Enrollment enroll = enrollment.get();
        double scoreObtenu = getMeilleurScoreQuizCours(user, etape.getCours());

        result.setProgressionCours(enroll.getProgress());
        result.setScoreObtenu(scoreObtenu);
        
        // Vérifier chaque condition avec gestion des NULL
        Integer scoreMinimum = etape.getScoreMinimum();
        result.setScoreMinimumRempli(scoreMinimum == null || scoreMinimum == 0 || scoreObtenu >= scoreMinimum);
        
        Integer completionRequis = etape.getPourcentageCompletionRequis();
        if (completionRequis == null || completionRequis == 0) {
            // Si NULL ou 0, on considère 100% par défaut
            result.setPourcentageCompletionRempli(enroll.getProgress() >= 100.0f);
        } else {
            result.setPourcentageCompletionRempli(enroll.getProgress() >= completionRequis);
        }
        
        Boolean quizObligatoires = etape.getQuizObligatoires();
        result.setQuizObligatoiresRemplis(quizObligatoires == null || !quizObligatoires || hasPassedRequiredQuizzes(user, etape.getCours()));

        return result;
    }

    /**
     * Classe pour encapsuler les résultats de validation d'une étape
     */
    public static class EtapeValidationResult {
        private float progressionCours;
        private double scoreObtenu;
        private boolean scoreMinimumRempli;
        private boolean pourcentageCompletionRempli;
        private boolean quizObligatoiresRemplis;

        // Getters et Setters
        public float getProgressionCours() { return progressionCours; }
        public void setProgressionCours(float progressionCours) { this.progressionCours = progressionCours; }

        public double getScoreObtenu() { return scoreObtenu; }
        public void setScoreObtenu(double scoreObtenu) { this.scoreObtenu = scoreObtenu; }

        public boolean isScoreMinimumRempli() { return scoreMinimumRempli; }
        public void setScoreMinimumRempli(boolean scoreMinimumRempli) { this.scoreMinimumRempli = scoreMinimumRempli; }

        public boolean isPourcentageCompletionRempli() { return pourcentageCompletionRempli; }
        public void setPourcentageCompletionRempli(boolean pourcentageCompletionRempli) { 
            this.pourcentageCompletionRempli = pourcentageCompletionRempli; 
        }

        public boolean isQuizObligatoiresRemplis() { return quizObligatoiresRemplis; }
        public void setQuizObligatoiresRemplis(boolean quizObligatoiresRemplis) { 
            this.quizObligatoiresRemplis = quizObligatoiresRemplis; 
        }
    }
}