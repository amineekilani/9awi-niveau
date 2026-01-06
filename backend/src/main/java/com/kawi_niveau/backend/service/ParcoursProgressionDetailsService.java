package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ApprenantProgressionResponse;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParcoursProgressionDetailsService {

    @Autowired
    private ParcoursInscriptionRepository parcoursInscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository parcoursEtapeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LeconCompletionRepository leconCompletionRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    public List<ApprenantProgressionResponse> getProgressionDetails(Long parcoursId) {
        return getProgressionDetails(parcoursId, null);
    }

    public List<ApprenantProgressionResponse> getProgressionDetails(Long parcoursId, Long formateurId) {
        // Récupérer toutes les inscriptions pour ce parcours
        List<ParcoursInscription> inscriptions = parcoursInscriptionRepository.findByParcoursId(parcoursId);

        // Récupérer les étapes du parcours
        List<ParcoursEtape> etapes = parcoursEtapeRepository.findByParcoursIdOrderByOrdreEtape(parcoursId);
        int totalEtapes = etapes.size();

        List<ApprenantProgressionResponse> progressions = new ArrayList<>();

        for (ParcoursInscription inscription : inscriptions) {
            User user = inscription.getUser();

            // Calculer la progression détaillée
            List<ApprenantProgressionResponse.EtapeProgressionDto> etapesProgression =
                    calculerProgressionEtapes(user.getId(), etapes);

            // Calculer les statistiques globales
            int etapesCompletees = (int) etapesProgression.stream()
                    .mapToInt(etape -> etape.getIsCompleted() ? 1 : 0)
                    .sum();

            int progressionPourcentage = totalEtapes > 0 ?
                    (etapesCompletees * 100) / totalEtapes : 0;

            // Calculer les points gagnés
            int pointsGagnes = calculerPointsGagnes(user.getId(), etapes);

            ApprenantProgressionResponse progression = new ApprenantProgressionResponse(
                    user.getId(),
                    user.getLastName(),
                    user.getFirstName(),
                    user.getEmail(),
                    inscription.getDateInscription(),
                    inscription.getDateCompletion(),
                    progressionPourcentage,
                    inscription.getEtapeCourante(),
                    totalEtapes,
                    pointsGagnes,
                    inscription.getIsCompleted(),
                    inscription.getCertificatGenere(),
                    inscription.getCertificatUrl()
            );

            progression.setEtapesProgression(etapesProgression);
            progressions.add(progression);
        }

        return progressions;
    }

    private List<ApprenantProgressionResponse.EtapeProgressionDto> calculerProgressionEtapes(
            Long userId, List<ParcoursEtape> etapes) {

        List<ApprenantProgressionResponse.EtapeProgressionDto> progressions = new ArrayList<>();

        for (ParcoursEtape etape : etapes) {
            Cours cours = etape.getCours();

            // Vérifier si l'utilisateur est inscrit au cours
            boolean inscritAuCours = enrollmentRepository
                    .existsByUserIdAndCoursId(userId, cours.getId());

            if (!inscritAuCours) {
                // Pas encore inscrit à cette étape
                progressions.add(new ApprenantProgressionResponse.EtapeProgressionDto(
                        etape.getId(),
                        cours.getTitre(),
                        etape.getOrdreEtape(),
                        false,
                        0,
                        null
                ));
                continue;
            }

            // Calculer la completion du cours
            boolean coursComplete = verifierCompletionCours(userId, cours.getId());
            int scoreObtenu = calculerScoreCours(userId, cours.getId());

            progressions.add(new ApprenantProgressionResponse.EtapeProgressionDto(
                    etape.getId(),
                    cours.getTitre(),
                    etape.getOrdreEtape(),
                    coursComplete,
                    scoreObtenu,
                    coursComplete ? java.time.LocalDateTime.now() : null // À améliorer avec vraie date
            ));
        }

        return progressions;
    }

    private boolean verifierCompletionCours(Long userId, Long coursId) {
        // Vérifier si toutes les leçons sont complétées
        List<Lecon> lecons = leconCompletionRepository.findLeconsByCoursId(coursId);
        if (lecons.isEmpty()) return false;

        long leconsCompletees = lecons.stream()
                .mapToLong(lecon -> leconCompletionRepository
                        .countByUserIdAndLeconId(userId, lecon.getId()))
                .sum();

        return leconsCompletees == lecons.size();
    }

    private int calculerScoreCours(Long userId, Long coursId) {
        // Calculer le score moyen des quiz du cours
        List<Quiz> quizzes = resultatQuizRepository.findQuizzesByCoursId(coursId);
        if (quizzes.isEmpty()) return 0;

        int totalScore = 0;
        int nombreQuizzes = 0;

        for (Quiz quiz : quizzes) {
            List<ResultatQuiz> resultats = resultatQuizRepository
                    .findByUserIdAndQuizIdOrderByDatePassedDesc(userId, quiz.getId());

            if (!resultats.isEmpty()) {
                // Prendre le meilleur score
                int meilleurScore = resultats.stream()
                        .mapToDouble(ResultatQuiz::getScore)
                        .mapToInt(score -> (int) Math.round(score))
                        .max()
                        .orElse(0);
                totalScore += meilleurScore;
                nombreQuizzes++;
            }
        }

        return nombreQuizzes > 0 ? totalScore / nombreQuizzes : 0;
    }

    private int calculerPointsGagnes(Long userId, List<ParcoursEtape> etapes) {
        int totalPoints = 0;

        for (ParcoursEtape etape : etapes) {
            // Points pour completion de l'étape
            if (verifierCompletionCours(userId, etape.getCours().getId())) {
                totalPoints += 50; // Points de base par étape

                // Bonus pour score élevé
                int score = calculerScoreCours(userId, etape.getCours().getId());
                if (score >= 90) totalPoints += 20;
                else if (score >= 80) totalPoints += 10;
            }
        }

        return totalPoints;
    }

    public Map<String, Object> getStatistiquesGlobales(Long parcoursId) {
        List<ParcoursInscription> inscriptions = parcoursInscriptionRepository.findByParcoursId(parcoursId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInscrits", inscriptions.size());

        int termines = (int) inscriptions.stream()
                .mapToInt(i -> i.getIsCompleted() ? 1 : 0).sum();
        stats.put("termines", termines);

        // Correction: "En cours" = Total inscrits - Terminés (logique simple et correcte)
        stats.put("enCours", inscriptions.size() - termines);

        // Correction: Certificats basés sur le champ certificatGenere
        stats.put("certificats", inscriptions.stream()
                .mapToInt(i -> i.getCertificatGenere() != null && i.getCertificatGenere() ? 1 : 0).sum());

        // Progression moyenne
        double progressionMoyenne = inscriptions.stream()
                .mapToDouble(ParcoursInscription::getProgressionPourcentage)
                .average()
                .orElse(0.0);
        stats.put("progressionMoyenne", (int) Math.round(progressionMoyenne));

        return stats;
    }
}