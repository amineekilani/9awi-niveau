package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleProgressResponse {
    private Long id;
    private String titre;
    private String contenu;
    private Integer ordre;
    private Long createdAt;
    private Long updatedAt;
    private Long coursId;
    
    // Progression des leçons
    private Integer totalLecons;
    private Integer leconsCompletees;
    private Float progressionLecons; // Pourcentage
    
    // Quiz
    private Boolean hasQuiz;
    private Long quizId;
    private String quizTitre;
    private Boolean quizPassed;
    private Double bestScore;
    private Integer totalAttempts;
}
