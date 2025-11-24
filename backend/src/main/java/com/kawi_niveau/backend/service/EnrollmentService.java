package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.EnrollmentRequest;
import com.kawi_niveau.backend.dto.EnrollmentResponse;
import com.kawi_niveau.backend.dto.LeconCompletionRequest;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.Lecon;
import com.kawi_niveau.backend.entity.LeconCompletion;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.EnrollmentRepository;
import com.kawi_niveau.backend.repository.LeconCompletionRepository;
import com.kawi_niveau.backend.repository.LeconRepository;
import com.kawi_niveau.backend.repository.ModuleRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LeconCompletionRepository leconCompletionRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private ModuleRepository moduleRepository;

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

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCours(cours);
        enrollment.setProgress(0.0f);

        enrollment = enrollmentRepository.save(enrollment);

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
        }

        enrollmentRepository.save(enrollment);
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

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        // Compter le nombre total de leçons
        List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(enrollment.getCours());
        int totalLecons = modules.stream()
                .mapToInt(module -> leconRepository.findByModuleOrderByOrdreAsc(module).size())
                .sum();

        int completedLecons = (int) leconCompletionRepository.countByEnrollment(enrollment);

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
                completedLecons
        );
    }
}
