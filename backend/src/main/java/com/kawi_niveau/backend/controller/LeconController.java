package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.LeconRequest;
import com.kawi_niveau.backend.dto.LeconResponse;
import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.service.LeconService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lecons")
@CrossOrigin(origins = "http://localhost:4200")
public class LeconController {

    @Autowired
    private LeconService leconService;

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<?> createLecon(@PathVariable Long moduleId, @Valid @RequestBody LeconRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            LeconResponse lecon = leconService.createLecon(moduleId, request, email);
            return ResponseEntity.ok(lecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/module/{moduleId}/with-file")
    public ResponseEntity<?> createLeconWithFile(
            @PathVariable Long moduleId,
            @RequestParam("titre") String titre,
            @RequestParam("typeContenu") String typeContenu,
            @RequestParam(value = "ordre", required = false) Integer ordre,
            @RequestParam(value = "duree", required = false) Integer duree,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            LeconResponse lecon = leconService.createLeconWithFile(moduleId, titre, typeContenu, ordre, duree, file, email);
            return ResponseEntity.ok(lecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLecon(@PathVariable Long id, @Valid @RequestBody LeconRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            LeconResponse lecon = leconService.updateLecon(id, request, email);
            return ResponseEntity.ok(lecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/file")
    public ResponseEntity<?> updateLeconFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            LeconResponse lecon = leconService.updateLeconFile(id, file, email);
            return ResponseEntity.ok(lecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLecon(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            leconService.deleteLecon(id, email);
            return ResponseEntity.ok(new MessageResponse("Leçon supprimée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<LeconResponse>> getLeconsByModule(@PathVariable Long moduleId) {
        List<LeconResponse> lecons = leconService.getLeconsByModule(moduleId);
        return ResponseEntity.ok(lecons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeconById(@PathVariable Long id) {
        try {
            LeconResponse lecon = leconService.getLeconById(id);
            return ResponseEntity.ok(lecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
