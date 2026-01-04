package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ChatbotRequest;
import com.kawi_niveau.backend.dto.ChatbotResponse;
import com.kawi_niveau.backend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ChatbotResponse chat(@RequestBody ChatbotRequest request) {
        return chatbotService.getAIResponse(request.getMessage());
    }
}
