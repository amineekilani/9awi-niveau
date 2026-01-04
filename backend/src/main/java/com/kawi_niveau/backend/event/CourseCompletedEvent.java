package com.kawi_niveau.backend.event;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Cours;
import org.springframework.context.ApplicationEvent;

public class CourseCompletedEvent extends ApplicationEvent {
    private final User user;
    private final Cours cours;
    private final float finalProgress;

    public CourseCompletedEvent(Object source, User user, Cours cours, float finalProgress) {
        super(source);
        this.user = user;
        this.cours = cours;
        this.finalProgress = finalProgress;
    }

    public User getUser() {
        return user;
    }

    public Cours getCours() {
        return cours;
    }

    public float getFinalProgress() {
        return finalProgress;
    }
}