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

    /**
     * Valide si une étape est débloquée pour un utilisateur
     */
    public boolean isEtapeDebloquee(ParcoursEtape etape, User user, List<ParcoursEtape> etapesPrecedentes) {
        // Si pas d'inscription au cours, étape verrouillée
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
        if (enrollment.isEmpty()) {
            return false;
        }

        // Pour un parcours flexible, toutes les étapes sont débloquées
        if (etape.getParcours().getTypeParcours() == TypeParcours.FLEXIBLE) {
            return true;
        }

        // Pour un parcours linéaire, vérifier l'ordre et les prérequis
        if (etape.getOrdreEtape() == 1) {
            return true; // La première étape est toujours débloquée
        }

        // Vérifier que toutes les étapes précédentes sont complètes
        for (ParcoursEtape etapePrecedente : etapesPrecedentes) {
            if (etapePrecedente.getOrdreEtape() < etape.getOrdreEtape()) {
                if (!isEtapeComplete(etapePrecedente, user)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Valide si une étape est complète pour un utilisateur
     */
    public boolean isEtapeComplete(ParcoursEtape etape, User user) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
        if (enrollment.isEmpty()) {
            return false;
        }

        Enrollment enroll = enrollment.get();
        
        // Vérifier le pourcentage de completion requis
        if (etape.getPourcentageCompletionRequis() > 0) {
            float progressionRequise = etape.getPourcentageCompletionRequis().floatValue();
            if (enroll.getProgress() < progressionRequise) {
                return false;
            }
        }

        // Vérifier le score minimum requis
        if (etape.getScoreMinimum() > 0) {
            double scoreObtenu = getMeilleurScoreQuizCours(user, etape.getCours());
            if (scoreObtenu < etape.getScoreMinimum()) {
                return false;
            }
        }

        // Vérifier les quiz obligatoires
        if (etape.getQuizObligatoires()) {
            if (!hasPassedRequiredQuizzes(user, etape.getCours())) {
                return false;
            }
        }

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
        
        for (Quiz quiz : quizzes) {
            Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
            
            // Considérer qu'un quiz est réussi avec un score >= 60%
            if (meilleurResultat.isEmpty() || meilleurResultat.get().getScore() < 60.0) {
                return false;
            }
        }

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
        
        // Vérifier chaque condition
        result.setScoreMinimumRempli(etape.getScoreMinimum() == 0 || scoreObtenu >= etape.getScoreMinimum());
        result.setPourcentageCompletionRempli(etape.getPourcentageCompletionRequis() == 0 || 
                                            enroll.getProgress() >= etape.getPourcentageCompletionRequis());
        result.setQuizObligatoiresRemplis(!etape.getQuizObligatoires() || hasPassedRequiredQuizzes(user, etape.getCours()));

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