package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping
    public ResponseEntity<?> testConnection() {
        return ResponseEntity.ok(new MessageResponse("Backend connecté avec succès"));
    }

    @GetMapping("/gamification")
    public ResponseEntity<?> testGamification() {
        return ResponseEntity.ok(new MessageResponse("Endpoint de gamification accessible"));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> testAdmin() {
        return ResponseEntity.ok(new MessageResponse("Endpoints admin accessibles"));
    }

    @GetMapping("/badges")
    public ResponseEntity<?> testBadges() {
        return ResponseEntity.ok(new MessageResponse("Endpoint badges accessible"));
    }
}