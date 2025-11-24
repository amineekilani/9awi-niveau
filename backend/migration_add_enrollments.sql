-- Migration pour ajouter les tables d'inscription et de suivi de progression

-- Table des inscriptions (enrollments)
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cours_id BIGINT NOT NULL,
    enrolled_at BIGINT NOT NULL,
    progress FLOAT NOT NULL DEFAULT 0.0,
    last_accessed_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_cours (user_id, cours_id)
);

-- Table des leçons complétées
CREATE TABLE IF NOT EXISTS lecon_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    lecon_id BIGINT NOT NULL,
    completed_at BIGINT NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (lecon_id) REFERENCES lecons(id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment_lecon (enrollment_id, lecon_id)
);

-- Index pour améliorer les performances
CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_cours ON enrollments(cours_id);
CREATE INDEX idx_lecon_completions_enrollment ON lecon_completions(enrollment_id);
CREATE INDEX idx_lecon_completions_lecon ON lecon_completions(lecon_id);
