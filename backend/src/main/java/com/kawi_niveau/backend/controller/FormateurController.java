package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.FormateurStatsResponse;
import com.kawi_niveau.backend.dto.ParcoursResponse;
import com.kawi_niveau.backend.dto.ParcoursProgressionStatsResponse;
import com.kawi_niveau.backend.service.FormateurService;
import com.kawi_niveau.backend.service.ParcoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formateur")
@CrossOrigin(origins = "*")
public class FormateurController {

    @Autowired
    private FormateurService formateurService;

    @Autowired
    private ParcoursService parcoursService;

    @GetMapping("/stats")
    public ResponseEntity<FormateurStatsResponse> getFormateurStats(Authentication authentication) {
        try {
            String email = authentication.getName();
            FormateurStatsResponse stats = formateurService.getFormateurStats(email);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}