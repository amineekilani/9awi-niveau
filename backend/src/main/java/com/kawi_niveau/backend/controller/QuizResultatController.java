package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.QuizSubmissionRequest;
import com.kawi_niveau.backend.dto.ResultatQuizResponse;
import com.kawi_niveau.backend.dto.QuizAttemptResponse;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.QuizResultatService;
import com.kawi_niveau.backend.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-resultats")
@CrossOrigin(origins = "http://localhost:4200")
public class QuizResultatController {

    @Autowired
    private QuizResultatService quizResultatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GamificationService gamificationService;

    @PostMapping("/quiz/{quizId}/submit")
    public ResponseEntity<ResultatQuizResponse> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            ResultatQuizResponse resultat = quizResultatService.submitQuiz(user.getId(), quizId, request);
            
            // Attribution automatique des XP et badges
            if (resultat.getScore() >= 50.0) { // Quiz réussi (seuil de 50%)
                gamificationService.onQuizPassed(user, resultat.getScore());
                System.out.println("Gamification: Quiz réussi pour " + user.getEmail() + " - Score: " + resultat.getScore());
            }
            
            return ResponseEntity.ok(resultat);
        } catch (Exception e) {
            System.err.println("Erreur lors de la soumission du quiz: " + e.getMessage());
            // Ne pas faire échouer la soumission à cause d'une erreur de gamification
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            ResultatQuizResponse resultat = quizResultatService.submitQuiz(user.getId(), quizId, request);
            return ResponseEntity.ok(resultat);
        }
    }

    @GetMapping("/quiz/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getUserQuizAttempts(
            @PathVariable Long quizId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<QuizAttemptResponse> attempts = quizResultatService.getUserQuizAttempts(user.getId(), quizId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/quiz/{quizId}/best-score")
    public ResponseEntity<QuizAttemptResponse> getBestScore(
            @PathVariable Long quizId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        QuizAttemptResponse bestScore = quizResultatService.getBestScore(user.getId(), quizId);
        return ResponseEntity.ok(bestScore);
    }

    @GetMapping("/cours/{coursId}/best-score")
    public ResponseEntity<QuizAttemptResponse> getBestScoreForCours(
            @PathVariable Long coursId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        QuizAttemptResponse bestScore = quizResultatService.getBestScoreForCours(user.getId(), coursId);
        return ResponseEntity.ok(bestScore);
    }

    @GetMapping("/{resultatId}")
    public ResponseEntity<ResultatQuizResponse> getResultatDetails(
            @PathVariable Long resultatId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        ResultatQuizResponse resultat = quizResultatService.getResultatDetails(user.getId(), resultatId);
        return ResponseEntity.ok(resultat);
    }
}
