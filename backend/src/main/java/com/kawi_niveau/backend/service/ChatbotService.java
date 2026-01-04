package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ChatbotResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.repository.CoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    public ChatbotResponse getAIResponse(String userMessage) {
        List<Cours> courses = coursRepository.findAll().stream()
                .filter(c -> !c.isArchived())
                .collect(Collectors.toList());

        String coursesContext = courses.stream()
                .map(c -> String.format("- %s: %s (Difficulté: %s, Catégorie: %s)",
                        c.getTitre(), c.getDescription(), c.getNiveauDifficulte(), c.getCategorie()))
                .collect(Collectors.joining("\n"));

        String systemPrompt = "Vous êtes un conseiller pédagogique expert pour la plateforme d'apprentissage '9awi Niveau'.\n"
                +
                "Votre objectif est d'aider les utilisateurs à trouver les cours les plus adaptés à leurs besoins.\n" +
                "Voici la liste des cours actuellement disponibles sur la plateforme :\n" +
                coursesContext + "\n\n" +
                "Instructions de formatage :\n" +
                "1. NE JAMAIS UTILISER DE GRAS (pas de double astérisques **).\n" +
                "2. Utilisez des tirets (-) pour lister les cours.\n" +
                "3. Séparez les différentes parties de votre réponse par des sauts de ligne clairs.\n" +
                "4. Présentez chaque cours de manière structurée : Nom du cours suivi d'une courte description.\n" +
                "5. Si vous proposez plusieurs cours, numérotez-les ou utilisez des puces.\n" +
                "6. Restez poli, professionnel et encourageant.\n" +
                "7. Répondez en français.";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model.trim());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return new ChatbotResponse((String) message.get("content"));
                }
            }
            return new ChatbotResponse("Désolé, je ne peux pas répondre pour le moment.");
        } catch (Exception e) {
            return new ChatbotResponse(
                    "Une erreur est survenue lors de la communication avec l'assistant : " + e.getMessage());
        }
    }
}
