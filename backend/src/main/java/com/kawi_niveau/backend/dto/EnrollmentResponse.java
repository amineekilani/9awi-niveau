package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Long userId;
    private Long coursId;
    private String coursTitle;
    private String coursDescription;
    private Long enrolledAt;
    private Float progress;
    private Long lastAccessedAt;
    private int totalLecons;
    private int completedLecons;
    private int totalQuiz;
    private int completedQuiz;
    private int passedQuiz;
}
