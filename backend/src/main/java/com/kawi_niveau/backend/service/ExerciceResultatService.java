package com.kawi_niveau.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciceResultatService {

    @Autowired
    private ResultatExerciceRepository resultatExerciceRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private ExerciceElementRepository exerciceElementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GamificationService gamificationService;

    @Transactional
    public ResultatExerciceResponse submitExercice(Long userId, Long exerciceId, ExerciceSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        List<ExerciceElement> elements = exerciceElementRepository.findByExerciceOrderByPositionOrdreAsc(exercice);
        
        if (elements.isEmpty()) {
            throw new RuntimeException("L'exercice ne contient aucun élément");
        }

        // Calculer le score
        int reponsesCorrectes = 0;
        List<ResultatExerciceResponse.ElementResultat> details = new ArrayList<>();

        for (ExerciceElement element : elements) {
            // Seuls les éléments avec une réponse attendue sont évalués
            if (element.getReponseCorrecte() != null && !element.getReponseCorrecte().isEmpty()) {
                String reponseUtilisateur = request.getReponses().get(element.getId());
                boolean correct = element.getReponseCorrecte().equalsIgnoreCase(reponseUtilisateur);
                
                if (correct) {
                    reponsesCorrectes++;
                }

                details.add(new ResultatExerciceResponse.ElementResultat(
                    element.getId(),
                    element.getContenu(),
                    reponseUtilisateur,
                    element.getReponseCorrecte(),
                    correct
                ));
            }
        }

        // Compter seulement les éléments évaluables
        int elementsEvaluables = (int) elements.stream()
                .filter(e -> e.getReponseCorrecte() != null && !e.getReponseCorrecte().isEmpty())
                .count();

        double score = elementsEvaluables > 0 ? ((double) reponsesCorrectes / elementsEvaluables) * 100 : 0;
        score = Math.round(score * 100.0) / 100.0; // Arrondir à 2 décimales

        // Sauvegarder le résultat
        ResultatExercice resultat = new ResultatExercice();
        resultat.setUser(user);
        resultat.setExercice(exercice);
        resultat.setScore(score);
        resultat.setNombreElements(elementsEvaluables);
        resultat.setReponsesCorrectes(reponsesCorrectes);
        resultat.setTempsPasse(request.getTempsPasse());

        // Sérialiser les détails en JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            resultat.setReponsesDetails(mapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            System.err.println("Erreur sérialisation détails: " + e.getMessage());
        }

        resultat = resultatExerciceRepository.save(resultat);

        // Gamification
        try {
            System.out.println("🎯 Attribution des récompenses de gamification pour exercice");
            gamificationService.awardXP(user, 8, "Exercice terminé: " + exercice.getTitre());
            
            if (score >= 60.0) {
                gamificationService.awardXP(user, 5, "Exercice réussi: " + exercice.getTitre());
            }
            
            System.out.println("✅ Gamification appliquée avec succès pour exercice");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur gamification exercice (non bloquante): " + e.getMessage());
        }

        return new ResultatExerciceResponse(
            resultat.getId(),
            user.getId(),
            exercice.getId(),
            exercice.getTitre(),
            score,
            resultat.getDatePassed(),
            elementsEvaluables,
            reponsesCorrectes,
            request.getTempsPasse(),
            details
        );
    }

    public List<ExerciceAttemptResponse> getUserExerciceAttempts(Long userId, Long exerciceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        List<ResultatExercice> resultats = resultatExerciceRepository.findByUserAndExerciceOrderByDatePassedDesc(user, exercice);

        return resultats.stream()
                .map(r -> new ExerciceAttemptResponse(
                    r.getId(),
                    r.getScore(),
                    r.getDatePassed(),
                    r.getReponsesCorrectes(),
                    r.getNombreElements()
                ))
                .collect(Collectors.toList());
    }

    public ExerciceAttemptResponse getBestScore(Long userId, Long exerciceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        return resultatExerciceRepository.findFirstByUserAndExerciceOrderByScoreDesc(user, exercice)
                .map(r -> new ExerciceAttemptResponse(
                    r.getId(),
                    r.getScore(),
                    r.getDatePassed(),
                    r.getReponsesCorrectes(),
                    r.getNombreElements()
                ))
                .orElse(null);
    }

    public ResultatExerciceResponse getResultatDetails(Long userId, Long resultatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ResultatExercice resultat = resultatExerciceRepository.findById(resultatId)
                .orElseThrow(() -> new RuntimeException("Résultat non trouvé"));

        if (!resultat.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé");
        }

        // Désérialiser les détails
        List<ResultatExerciceResponse.ElementResultat> details = new ArrayList<>();
        if (resultat.getReponsesDetails() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                details = mapper.readValue(resultat.getReponsesDetails(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, ResultatExerciceResponse.ElementResultat.class));
            } catch (JsonProcessingException e) {
                System.err.println("Erreur désérialisation détails: " + e.getMessage());
            }
        }

        return new ResultatExerciceResponse(
            resultat.getId(),
            resultat.getUser().getId(),
            resultat.getExercice().getId(),
            resultat.getExercice().getTitre(),
            resultat.getScore(),
            resultat.getDatePassed(),
            resultat.getNombreElements(),
            resultat.getReponsesCorrectes(),
            resultat.getTempsPasse(),
            details
        );
    }
}