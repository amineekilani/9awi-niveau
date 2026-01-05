package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ApprenantProgressionResponse;
import com.kawi_niveau.backend.dto.EnrollmentRequest;
import com.kawi_niveau.backend.dto.EnrollmentResponse;
import com.kawi_niveau.backend.dto.LeconCompletionRequest;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.event.CourseCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeconCompletionRepository leconCompletionRepository;

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private com.kawi_niveau.backend.repository.QuizRepository quizRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ParcoursIntegrationService parcoursIntegrationService;

    @Autowired
    private GamificationService gamificationService;

    @Transactional
    public EnrollmentResponse enrollInCourse(Long userId, EnrollmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Vérifier si l'utilisateur est déjà inscrit
        if (enrollmentRepository.existsByUserAndCours(user, cours)) {
            throw new RuntimeException("Vous êtes déjà inscrit à ce cours");
        }

        // Créer un nouveau enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCours(cours);
        enrollment.setProgress(0.0f);

        enrollment = enrollmentRepository.save(enrollment);

        // Déclencher l'événement d'inscription pour la gamification
        try {
            gamificationService.onCourseEnrollment(user);
        } catch (Exception e) {
            System.err.println("Erreur lors de la gamification d'inscription: " + e.getMessage());
        }

        return mapToResponse(enrollment);
    }

    public List<EnrollmentResponse> getUserEnrollments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        return enrollments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EnrollmentResponse getEnrollmentDetails(Long userId, Long coursId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        Enrollment enrollment = enrollmentRepository.findByUserAndCours(user, cours)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        // Mettre à jour le dernier accès
        enrollment.setLastAccessedAt(System.currentTimeMillis());
        enrollmentRepository.save(enrollment);

        return mapToResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse markLeconAsCompleted(Long userId, Long coursId, LeconCompletionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        Enrollment enrollment = enrollmentRepository.findByUserAndCours(user, cours)
                .orElseThrow(() -> new RuntimeException("Vous devez être inscrit à ce cours"));

        Lecon lecon = leconRepository.findById(request.getLeconId())
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        // Vérifier que la leçon appartient bien au cours
        if (!lecon.getModule().getCours().getId().equals(coursId)) {
            throw new RuntimeException("Cette leçon n'appartient pas à ce cours");
        }

        // Vérifier si la leçon n'est pas déjà complétée
        if (!leconCompletionRepository.existsByEnrollmentAndLecon(enrollment, lecon)) {
            LeconCompletion completion = new LeconCompletion();
            completion.setEnrollment(enrollment);
            completion.setLecon(lecon);
            leconCompletionRepository.save(completion);

            // Déclencher l'événement de leçon terminée pour la gamification
            try {
                gamificationService.onLessonCompleted(enrollment.getUser());
            } catch (Exception e) {
                System.err.println("Erreur lors de la gamification de leçon terminée: " + e.getMessage());
            }

            // Recalculer la progression
            updateProgress(enrollment);
        }

        return mapToResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse unmarkLeconAsCompleted(Long userId, Long coursId, Long leconId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        Enrollment enrollment = enrollmentRepository.findByUserAndCours(user, cours)
                .orElseThrow(() -> new RuntimeException("Vous devez être inscrit à ce cours"));

        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        LeconCompletion completion = leconCompletionRepository.findByEnrollmentAndLecon(enrollment, lecon)
                .orElse(null);
        if (completion != null) {
            leconCompletionRepository.delete(completion);
            updateProgress(enrollment);
        }

        return mapToResponse(enrollment);
    }

    public List<Long> getCompletedLeconIds(Long userId, Long coursId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        Enrollment enrollment = enrollmentRepository.findByUserAndCours(user, cours)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        List<LeconCompletion> completions = leconCompletionRepository.findByEnrollment(enrollment);
        return completions.stream()
                .map(completion -> completion.getLecon().getId())
                .collect(Collectors.toList());
    }

    public List<ApprenantProgressionResponse> getApprenantsProgression(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        List<Enrollment> enrollments = enrollmentRepository.findByCours(cours);
        return enrollments.stream()
                .map(this::mapToApprenantProgression)
                .collect(Collectors.toList());
    }

    private void updateProgress(Enrollment enrollment) {
        // Compter le nombre total de leçons dans le cours
        List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(enrollment.getCours());
        long totalLecons = modules.stream()
                .mapToLong(module -> leconRepository.findByModuleOrderByOrdreAsc(module).size())
                .sum();

        if (totalLecons == 0) {
            enrollment.setProgress(0.0f);
        } else {
            long completedLecons = leconCompletionRepository.countByEnrollment(enrollment);
            float progress = (float) completedLecons / totalLecons * 100;
            enrollment.setProgress(Math.round(progress * 100) / 100.0f); // Arrondir à 2 décimales

            // Vérifier si le cours est terminé (100% de progression)
            if (progress >= 100.0f) {
                // Déclencher l'événement de cours terminé pour la gamification
                try {
                    gamificationService.onCourseCompleted(enrollment.getUser());
                    // ✅ NOUVEAU: Publier l'événement pour les parcours
                    eventPublisher.publishEvent(new CourseCompletedEvent(this, enrollment.getUser(), enrollment.getCours(), progress));
                    
                    // ✅ INTÉGRATION PARCOURS: Mise à jour directe de la progression des parcours
                    System.out.println("🔄 Intégration parcours: Cours terminé par " + enrollment.getUser().getEmail() + ", cours " + enrollment.getCours().getTitre());
                    parcoursIntegrationService.onCoursProgressUpdated(enrollment.getUser().getId(), enrollment.getCours().getId());
                    System.out.println("✅ Progression parcours mise à jour après completion cours: " + enrollment.getCours().getTitre());
                } catch (Exception e) {
                    // Log l'erreur mais ne pas faire échouer la mise à jour de progression
                    System.err.println("Erreur lors de la gamification: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        // Compter le nombre total de leçons
        List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(enrollment.getCours());
        int totalLecons = modules.stream()
                .mapToInt(module -> leconRepository.findByModuleOrderByOrdreAsc(module).size())
                .sum();
        int completedLecons = (int) leconCompletionRepository.countByEnrollment(enrollment);

        // Compter les quiz
        int totalQuiz = (int) modules.stream()
                .filter(module -> quizRepository.findByModule(module).isPresent())
                .count();

        // Compter les quiz tentés (au moins une tentative)
        int completedQuiz = (int) modules.stream()
                .filter(module -> {
                    return quizRepository.findByModule(module)
                            .map(quiz -> !resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(enrollment.getUser(), quiz).isEmpty())
                            .orElse(false);
                })
                .count();

        // Compter les quiz réussis (score >= 50%)
        int passedQuiz = (int) modules.stream()
                .filter(module -> {
                    return quizRepository.findByModule(module)
                            .map(quiz -> resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(enrollment.getUser(), quiz)
                                    .stream()
                                    .anyMatch(resultat -> resultat.getScore() >= 50.0))
                            .orElse(false);
                })
                .count();

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getCours().getId(),
                enrollment.getCours().getTitre(),
                enrollment.getCours().getDescription(),
                enrollment.getEnrolledAt(),
                enrollment.getProgress(),
                enrollment.getLastAccessedAt(),
                totalLecons,
                completedLecons,
                totalQuiz,
                completedQuiz,
                passedQuiz
        );
    }

    private com.kawi_niveau.backend.dto.ApprenantProgressionResponse mapToApprenantProgression(Enrollment enrollment) {
        User user = enrollment.getUser();
        Cours cours = enrollment.getCours();

        // Récupérer les modules du cours
        List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(cours);

        // Calculer la progression globale
        int totalLecons = modules.stream()
                .mapToInt(module -> leconRepository.findByModuleOrderByOrdreAsc(module).size())
                .sum();
        int leconsCompletees = (int) leconCompletionRepository.countByEnrollment(enrollment);

        // Progression par module
        List<com.kawi_niveau.backend.dto.ApprenantProgressionResponse.ModuleProgressionDetail> modulesProgression = modules.stream()
                .map(module -> {
                    List<Lecon> lecons = leconRepository.findByModuleOrderByOrdreAsc(module);
                    int totalLeconsModule = lecons.size();
                    long leconsCompleteesModule = lecons.stream()
                            .filter(lecon -> leconCompletionRepository.existsByEnrollmentAndLecon(enrollment, lecon))
                            .count();
                    float progressionModule = totalLeconsModule > 0 ? (float) leconsCompleteesModule / totalLeconsModule * 100 : 0;

                    // Vérifier s'il y a un quiz pour ce module
                    com.kawi_niveau.backend.dto.ApprenantProgressionResponse.QuizResultatDetail quizDetail = null;
                    com.kawi_niveau.backend.entity.Quiz quiz = quizRepository.findByModule(module).orElse(null);
                    if (quiz != null) {
                        List<com.kawi_niveau.backend.entity.ResultatQuiz> resultats = resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(user, quiz);
                        if (!resultats.isEmpty()) {
                            double meilleurScore = resultats.stream()
                                    .mapToDouble(com.kawi_niveau.backend.entity.ResultatQuiz::getScore)
                                    .max()
                                    .orElse(0.0);
                            com.kawi_niveau.backend.entity.ResultatQuiz dernierResultat = resultats.get(0);
                            quizDetail = new com.kawi_niveau.backend.dto.ApprenantProgressionResponse.QuizResultatDetail(
                                    quiz.getId(),
                                    quiz.getTitre(),
                                    meilleurScore,
                                    resultats.size(),
                                    dernierResultat.getDatePassed(),
                                    meilleurScore >= 50.0 // Score minimum par défaut de 50%
                            );
                        }
                    }

                    return new com.kawi_niveau.backend.dto.ApprenantProgressionResponse.ModuleProgressionDetail(
                            module.getId(),
                            module.getTitre(),
                            totalLeconsModule,
                            (int) leconsCompleteesModule,
                            progressionModule,
                            quizDetail
                    );
                })
                .collect(Collectors.toList());

        // Tous les résultats de quiz du cours
        List<com.kawi_niveau.backend.dto.ApprenantProgressionResponse.QuizResultatDetail> quizResultats = modules.stream()
                .map(module -> {
                    com.kawi_niveau.backend.entity.Quiz quiz = quizRepository.findByModule(module).orElse(null);
                    if (quiz == null) {
                        return null;
                    }

                    List<com.kawi_niveau.backend.entity.ResultatQuiz> resultats = resultatQuizRepository.findByUserAndQuizOrderByDatePassedDesc(user, quiz);
                    if (resultats.isEmpty()) {
                        return null;
                    }

                    double meilleurScore = resultats.stream()
                            .mapToDouble(com.kawi_niveau.backend.entity.ResultatQuiz::getScore)
                            .max()
                            .orElse(0.0);
                    com.kawi_niveau.backend.entity.ResultatQuiz dernierResultat = resultats.get(0);

                    return new com.kawi_niveau.backend.dto.ApprenantProgressionResponse.QuizResultatDetail(
                            quiz.getId(),
                            quiz.getTitre(),
                            meilleurScore,
                            resultats.size(),
                            dernierResultat.getDatePassed(),
                            meilleurScore >= 50.0 // Score minimum par défaut de 50%
                    );
                })
                .filter(detail -> detail != null)
                .collect(Collectors.toList());

        return new com.kawi_niveau.backend.dto.ApprenantProgressionResponse(
                user.getId(),
                user.getLastName(),
                user.getFirstName(),
                user.getEmail(),
                user.getProfileImage(),
                enrollment.getProgress(),
                totalLecons,
                leconsCompletees,
                enrollment.getEnrolledAt(),
                enrollment.getLastAccessedAt(),
                modulesProgression,
                quizResultats
        );
    }
}