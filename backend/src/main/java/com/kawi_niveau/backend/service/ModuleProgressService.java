package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ModuleProgressResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.Lecon;
import com.kawi_niveau.backend.entity.Quiz;
import com.kawi_niveau.backend.entity.ResultatQuiz;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.EnrollmentRepository;
import com.kawi_niveau.backend.repository.LeconCompletionRepository;
import com.kawi_niveau.backend.repository.LeconRepository;
import com.kawi_niveau.backend.repository.ModuleRepository;
import com.kawi_niveau.backend.repository.QuizRepository;
import com.kawi_niveau.backend.repository.ResultatQuizRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ModuleProgressService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LeconCompletionRepository leconCompletionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoursRepository coursRepository;

    public List<ModuleProgressResponse> getModulesWithProgress(Long userId, Long coursId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Récupérer l'enrollment
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndCours(user, cours);

        List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(cours);
        List<ModuleProgressResponse> responses = new ArrayList<>();

        for (com.kawi_niveau.backend.entity.Module module : modules) {
            ModuleProgressResponse response = new ModuleProgressResponse();
            response.setId(module.getId());
            response.setTitre(module.getTitre());
            response.setContenu(module.getContenu());
            response.setOrdre(module.getOrdre());
            response.setCreatedAt(module.getCreatedAt());
            response.setUpdatedAt(module.getUpdatedAt());
            response.setCoursId(module.getCours().getId());

            // Progression des leçons
            List<Lecon> lecons = leconRepository.findByModuleOrderByOrdreAsc(module);
            response.setTotalLecons(lecons.size());

            if (enrollmentOpt.isPresent()) {
                Enrollment enrollment = enrollmentOpt.get();
                
                // Compter les leçons complétées de ce module
                long leconsCompletees = lecons.stream()
                        .filter(lecon -> leconCompletionRepository.existsByEnrollmentAndLecon(enrollment, lecon))
                        .count();
                
                response.setLeconsCompletees((int) leconsCompletees);
                
                if (lecons.size() > 0) {
                    float progression = ((float) leconsCompletees / lecons.size()) * 100;
                    response.setProgressionLecons(Math.round(progression * 100) / 100.0f);
                } else {
                    response.setProgressionLecons(0.0f);
                }
            } else {
                response.setLeconsCompletees(0);
                response.setProgressionLecons(0.0f);
            }

            // Informations sur le quiz
            Optional<Quiz> quizOpt = quizRepository.findByModule(module);
            if (quizOpt.isPresent()) {
                Quiz quiz = quizOpt.get();
                response.setHasQuiz(true);
                response.setQuizId(quiz.getId());
                response.setQuizTitre(quiz.getTitre());

                if (enrollmentOpt.isPresent()) {
                    // Récupérer les résultats du quiz
                    List<ResultatQuiz> resultats = resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(user, quiz);
                    response.setTotalAttempts(resultats.size());

                    if (!resultats.isEmpty()) {
                        response.setQuizPassed(true);
                        // Meilleur score
                        Optional<ResultatQuiz> bestResult = resultatQuizRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                        bestResult.ifPresent(resultat -> response.setBestScore(resultat.getScore()));
                    } else {
                        response.setQuizPassed(false);
                        response.setBestScore(null);
                    }
                } else {
                    response.setQuizPassed(false);
                    response.setTotalAttempts(0);
                    response.setBestScore(null);
                }
            } else {
                response.setHasQuiz(false);
                response.setQuizId(null);
                response.setQuizTitre(null);
                response.setQuizPassed(false);
                response.setTotalAttempts(0);
                response.setBestScore(null);
            }

            responses.add(response);
        }

        return responses;
    }
}
