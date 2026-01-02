package com.kawi_niveau.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RestController
@RequestMapping("/api/init")
@CrossOrigin(origins = "http://localhost:4200")
public class InitController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/gamification-tables")
    public ResponseEntity<String> createGamificationTables() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Créer les tables de gamification
            String[] sqlStatements = {
                "DROP TABLE IF EXISTS user_challenges",
                "DROP TABLE IF EXISTS challenges", 
                "DROP TABLE IF EXISTS user_badges",
                "DROP TABLE IF EXISTS badges",
                "DROP TABLE IF EXISTS user_xp",
                "DROP TABLE IF EXISTS levels",
                
                "CREATE TABLE levels (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "level INT NOT NULL UNIQUE," +
                "xp_required INT NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "created_at BIGINT" +
                ")",

                "CREATE TABLE badges (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL UNIQUE," +
                "description TEXT," +
                "icon_url VARCHAR(500)," +
                "criteria_type VARCHAR(50) NOT NULL," +
                "criteria_value INT," +
                "is_active BOOLEAN DEFAULT TRUE," +
                "created_at BIGINT," +
                "updated_at BIGINT" +
                ")",

                "CREATE TABLE user_xp (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL UNIQUE," +
                "total_xp INT DEFAULT 0," +
                "current_level INT DEFAULT 1," +
                "xp_to_next_level INT DEFAULT 100," +
                "last_updated BIGINT," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")",

                "CREATE TABLE user_badges (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "badge_id BIGINT NOT NULL," +
                "earned_at BIGINT," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE," +
                "UNIQUE KEY unique_user_badge (user_id, badge_id)" +
                ")",

                "CREATE TABLE challenges (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "challenge_type VARCHAR(50) NOT NULL," +
                "target_value INT NOT NULL," +
                "xp_reward INT NOT NULL," +
                "start_date BIGINT," +
                "end_date BIGINT," +
                "is_active BOOLEAN DEFAULT TRUE," +
                "created_at BIGINT," +
                "updated_at BIGINT" +
                ")",

                "CREATE TABLE user_challenges (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "challenge_id BIGINT NOT NULL," +
                "current_progress INT DEFAULT 0," +
                "is_completed BOOLEAN DEFAULT FALSE," +
                "completed_at BIGINT," +
                "joined_at BIGINT," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE," +
                "UNIQUE KEY unique_user_challenge (user_id, challenge_id)" +
                ")",

                "INSERT INTO levels (level, xp_required, name, description) VALUES " +
                "(1, 0, 'Débutant', 'Bienvenue dans votre parcours d\\'apprentissage !')," +
                "(2, 100, 'Apprenti', 'Vous commencez à maîtriser les bases')," +
                "(3, 250, 'Étudiant', 'Vous progressez bien dans vos études')," +
                "(4, 500, 'Avancé', 'Vous avez acquis de solides compétences')," +
                "(5, 1000, 'Expert', 'Vous maîtrisez votre domaine')",

                "INSERT INTO badges (name, description, icon_url, criteria_type, criteria_value, is_active) VALUES " +
                "('Premier Quiz', 'Réussissez votre premier quiz', '/icons/first-quiz.svg', 'QUIZ_PASSED', 1, TRUE)," +
                "('Perfectionniste', 'Obtenez un score parfait à un quiz', '/icons/perfect-score.svg', 'PERFECT_SCORE', 1, TRUE)," +
                "('Expert Quiz', 'Réussissez 10 quiz', '/icons/quiz-expert.svg', 'QUIZ_PASSED', 10, TRUE)"
            };

            for (String sql : sqlStatements) {
                statement.execute(sql);
            }

            return ResponseEntity.ok("Tables de gamification créées avec succès!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création des tables: " + e.getMessage());
        }
    }

    @PostMapping("/fix-duplicates")
    public ResponseEntity<String> fixDuplicates() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Supprimer les doublons dans user_xp
            String deleteDuplicates = "DELETE ux1 FROM user_xp ux1 " +
                                    "INNER JOIN user_xp ux2 " +
                                    "WHERE ux1.id < ux2.id AND ux1.user_id = ux2.user_id";
            
            int deletedRows = statement.executeUpdate(deleteDuplicates);
            
            return ResponseEntity.ok("Doublons supprimés: " + deletedRows + " enregistrements");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression des doublons: " + e.getMessage());
        }
    }
}