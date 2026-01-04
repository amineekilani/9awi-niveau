package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.QuizSubmissionRequest;
import com.kawi_niveau.backend.dto.ResultatQuizResponse;
import com.kawi_niveau.backend.dto.QuizAttemptResponse;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.event.QuizCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizResultatService {

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public ResultatQuizResponse submitQuiz(Long userId, Long quizId, QuizSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        List<Question> questions = questionRepository.findByQuizOrderByOrdreAsc(quiz);
        
        if (questions.isEmpty()) {
            throw new RuntimeException("Le quiz ne contient aucune question");
        }

        // Calculer le score
        int reponsesCorrectes = 0;
        List<ResultatQuizResponse.QuestionResultat> details = new ArrayList<>();

        for (Question question : questions) {
            String reponseUtilisateur = request.getReponses().get(question.getId());
            boolean correct = question.getCorrectAnswer().equals(reponseUtilisateur);
            
            if (correct) {
                reponsesCorrectes++;
            }

            details.add(new ResultatQuizResponse.QuestionResultat(
                question.getId(),
                question.getQuestion(),
                reponseUtilisateur,
                question.getCorrectAnswer(),
                correct
            ));
        }

        double score = ((double) reponsesCorrectes / questions.size()) * 100;
        score = Math.round(score * 100.0) / 100.0; // Arrondir à 2 décimales

        // Sauvegarder le résultat
        ResultatQuiz resultat = new ResultatQuiz();
        resultat.setUser(user);
        resultat.setQuiz(quiz);
        resultat.setScore(score);
        resultat.setNombreQuestions(questions.size());
        resultat.setReponsesCorrectes(reponsesCorrectes);
        resultat.setTempsPasse(request.getTempsPasse());

        resultat = resultatQuizRepository.save(resultat);

        // Déclencher les événements de gamification
        try {
            gamificationService.onQuizPassed(user, score);
            
            // ✅ NOUVEAU: Publier l'événement pour les parcours
            eventPublisher.publishEvent(new QuizCompletedEvent(this, user, quiz, score));
            
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la soumission du quiz
            System.err.println("Erreur lors de la gamification: " + e.getMessage());
        }

        return new ResultatQuizResponse(
            resultat.getId(),
            user.getId(),
            quiz.getId(),
            quiz.getTitre(),
            score,
            resultat.getDatePassed(),
            questions.size(),
            reponsesCorrectes,
            request.getTempsPasse(),
            details
        );
    }

    public List<QuizAttemptResponse> getUserQuizAttempts(Long userId, Long quizId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        List<ResultatQuiz> resultats = resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(user, quiz);

        return resultats.stream()
                .map(r -> new QuizAttemptResponse(
                    r.getId(),
                    r.getScore(),
                    r.getDatePassed(),
                    r.getReponsesCorrectes(),
                    r.getNombreQuestions()
                ))
                .collect(Collectors.toList());
    }

    public ResultatQuizResponse getResultatDetails(Long userId, Long resultatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ResultatQuiz resultat = resultatQuizRepository.findById(resultatId)
                .orElseThrow(() -> new RuntimeException("Résultat non trouvé"));

        if (!resultat.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé");
        }

        // Note: Les détails des réponses ne sont pas stockés, on retourne juste le résumé
        return new ResultatQuizResponse(
            resultat.getId(),
            resultat.getUser().getId(),
            resultat.getQuiz().getId(),
            resultat.getQuiz().getTitre(),
            resultat.getScore(),
            resultat.getDatePassed(),
            resultat.getNombreQuestions(),
            resultat.getReponsesCorrectes(),
            resultat.getTempsPasse(),
            new ArrayList<>() // Pas de détails stockés
        );
    }

    public QuizAttemptResponse getBestScore(Long userId, Long quizId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        return resultatQuizRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz)
                .map(r -> new QuizAttemptResponse(
                    r.getId(),
                    r.getScore(),
                    r.getDatePassed(),
                    r.getReponsesCorrectes(),
                    r.getNombreQuestions()
                ))
                .orElse(null);
    }

    public QuizAttemptResponse getBestScoreForCours(Long userId, Long coursId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer tous les quiz du cours et trouver le meilleur score
        List<Quiz> quizzes = quizRepository.findAll().stream()
                .filter(q -> q.getModule().getCours().getId().equals(coursId))
                .collect(Collectors.toList());

        double meilleurScore = 0.0;
        QuizAttemptResponse meilleurResultat = null;

        for (Quiz quiz : quizzes) {
            QuizAttemptResponse resultat = resultatQuizRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz)
                    .map(r -> new QuizAttemptResponse(
                        r.getId(),
                        r.getScore(),
                        r.getDatePassed(),
                        r.getReponsesCorrectes(),
                        r.getNombreQuestions()
                    ))
                    .orElse(null);

            if (resultat != null && resultat.getScore() > meilleurScore) {
                meilleurScore = resultat.getScore();
                meilleurResultat = resultat;
            }
        }

        return meilleurResultat;
    }
}
