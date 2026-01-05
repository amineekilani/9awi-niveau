-- Migration pour ajouter la table des résultats d'exercices

CREATE TABLE IF NOT EXISTS resultat_exercice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercice_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    date_passed BIGINT NOT NULL,
    nombre_elements INT,
    reponses_correctes INT,
    temps_passe INT,
    reponses_details TEXT, -- JSON des réponses détaillées
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercice(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_resultat_exercice_user ON resultat_exercice(user_id);
CREATE INDEX idx_resultat_exercice_exercice ON resultat_exercice(exercice_id);
CREATE INDEX idx_resultat_exercice_date ON resultat_exercice(date_passed);
CREATE INDEX idx_resultat_exercice_score ON resultat_exercice(score);