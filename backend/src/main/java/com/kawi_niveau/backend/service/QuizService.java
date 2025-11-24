package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.QuestionRequest;
import com.kawi_niveau.backend.dto.QuestionResponse;
import com.kawi_niveau.backend.dto.QuizRequest;
import com.kawi_niveau.backend.dto.QuizResponse;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.ModuleRepository;
import com.kawi_niveau.backend.repository.QuestionRepository;
import com.kawi_niveau.backend.repository.QuizRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public QuizResponse createQuiz(Long moduleId, QuizRequest request, String email) {
        com.kawi_niveau.backend.entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que le formateur est propriétaire du cours
        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à créer un quiz pour ce module");
        }

        // Vérifier qu'il n'y a pas déjà un quiz pour ce module
        if (quizRepository.findByModule(module).isPresent()) {
            throw new RuntimeException("Un quiz existe déjà pour ce module");
        }

        Quiz quiz = new Quiz();
        quiz.setTitre(request.getTitre());
        quiz.setDescription(request.getDescription());
        quiz.setModule(module);

        Quiz savedQuiz = quizRepository.save(quiz);

        // Ajouter les questions si présentes
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (QuestionRequest qr : request.getQuestions()) {
                Question question = new Question();
                question.setQuestion(qr.getQuestion());
                question.setOptions(qr.getOptions());
                question.setCorrectAnswer(qr.getCorrectAnswer());
                question.setOrdre(qr.getOrdre());
                question.setQuiz(savedQuiz);
                questionRepository.save(question);
            }
        }

        return mapToResponse(savedQuiz);
    }

    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!quiz.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce quiz");
        }

        quiz.setTitre(request.getTitre());
        quiz.setDescription(request.getDescription());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return mapToResponse(updatedQuiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!quiz.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce quiz");
        }

        quizRepository.delete(quiz);
    }

    public QuizResponse getQuizByModuleId(Long moduleId) {
        com.kawi_niveau.backend.entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        Quiz quiz = quizRepository.findByModule(module)
                .orElse(null);

        return quiz != null ? mapToResponse(quiz) : null;
    }

    public QuizResponse getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));
        return mapToResponse(quiz);
    }

    @Transactional
    public QuestionResponse addQuestion(Long quizId, QuestionRequest request, String email) {
        System.out.println("=== Ajout de question ===");
        System.out.println("Quiz ID: " + quizId);
        System.out.println("Question: " + request.getQuestion());
        System.out.println("Options: " + request.getOptions());
        System.out.println("Réponse correcte: " + request.getCorrectAnswer());
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!quiz.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter une question à ce quiz");
        }

        Question question = new Question();
        question.setQuestion(request.getQuestion());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setOrdre(request.getOrdre());
        question.setQuiz(quiz);

        System.out.println("Question avant sauvegarde - Options: " + question.getOptions());
        Question savedQuestion = questionRepository.save(question);
        System.out.println("Question sauvegardée - ID: " + savedQuestion.getId());
        System.out.println("Question sauvegardée - Options: " + savedQuestion.getOptions());
        
        return mapQuestionToResponse(savedQuestion);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request, String email) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question non trouvée"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!question.getQuiz().getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette question");
        }

        question.setQuestion(request.getQuestion());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setOrdre(request.getOrdre());

        Question updatedQuestion = questionRepository.save(question);
        return mapQuestionToResponse(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId, String email) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question non trouvée"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!question.getQuiz().getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette question");
        }

        questionRepository.delete(question);
    }

    private QuizResponse mapToResponse(Quiz quiz) {
        List<Question> questions = questionRepository.findByQuizOrderByOrdreAsc(quiz);
        List<QuestionResponse> questionResponses = questions.stream()
                .map(this::mapQuestionToResponse)
                .collect(Collectors.toList());

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitre(),
                quiz.getDescription(),
                quiz.getModule().getId(),
                questionResponses,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    private QuestionResponse mapQuestionToResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuestion(),
                question.getOptions(),
                question.getCorrectAnswer(),
                question.getOrdre(),
                question.getCreatedAt()
        );
    }
}
