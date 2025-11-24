package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:4200")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<?> createQuiz(
            @PathVariable Long moduleId,
            @Valid @RequestBody QuizRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            QuizResponse quiz = quizService.createQuiz(moduleId, request, email);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<?> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            QuizResponse quiz = quizService.updateQuiz(quizId, request, email);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId, Authentication authentication) {
        try {
            String email = authentication.getName();
            quizService.deleteQuiz(quizId, email);
            return ResponseEntity.ok(new MessageResponse("Quiz supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getQuizByModuleId(@PathVariable Long moduleId) {
        try {
            QuizResponse quiz = quizService.getQuizByModuleId(moduleId);
            if (quiz == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuizById(@PathVariable Long quizId) {
        try {
            QuizResponse quiz = quizService.getQuizById(quizId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{quizId}/question")
    public ResponseEntity<?> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            QuestionResponse question = quizService.addQuestion(quizId, request, email);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/question/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            QuestionResponse question = quizService.updateQuestion(questionId, request, email);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
        try {
            String email = authentication.getName();
            quizService.deleteQuestion(questionId, email);
            return ResponseEntity.ok(new MessageResponse("Question supprimée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
