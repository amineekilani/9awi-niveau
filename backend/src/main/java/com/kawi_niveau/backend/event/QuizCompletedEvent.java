package com.kawi_niveau.backend.event;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Quiz;
import org.springframework.context.ApplicationEvent;

public class QuizCompletedEvent extends ApplicationEvent {
    private final User user;
    private final Quiz quiz;
    private final double score;

    public QuizCompletedEvent(Object source, User user, Quiz quiz, double score) {
        super(source);
        this.user = user;
        this.quiz = quiz;
        this.score = score;
    }

    public User getUser() {
        return user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public double getScore() {
        return score;
    }
}