-- Migration pour ajouter la table des résultats de quiz

CREATE TABLE IF NOT EXISTS resultat_quiz (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    date_passed BIGINT NOT NULL,
    nombre_questions INT,
    reponses_correctes INT,
    temps_passe INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_resultat_quiz_user ON resultat_quiz(user_id);
CREATE INDEX idx_resultat_quiz_quiz ON resultat_quiz(quiz_id);
CREATE INDEX idx_resultat_quiz_date ON resultat_quiz(date_passed);
CREATE INDEX idx_resultat_quiz_score ON resultat_quiz(score);
