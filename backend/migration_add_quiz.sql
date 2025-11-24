-- Migration pour ajouter les tables Quiz et Question

-- Table Quiz
CREATE TABLE IF NOT EXISTS quiz (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    module_id BIGINT NOT NULL,
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE,
    UNIQUE KEY unique_module_quiz (module_id)
);

-- Table Question
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_answer VARCHAR(500) NOT NULL,
    quiz_id BIGINT NOT NULL,
    ordre INT,
    created_at BIGINT,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_quiz_module ON quiz(module_id);
CREATE INDEX idx_question_quiz ON question(quiz_id);
CREATE INDEX idx_question_ordre ON question(ordre);
