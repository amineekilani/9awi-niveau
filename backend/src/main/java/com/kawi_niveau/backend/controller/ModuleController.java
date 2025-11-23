package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.dto.ModuleRequest;
import com.kawi_niveau.backend.dto.ModuleResponse;
import com.kawi_niveau.backend.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @PostMapping("/cours/{coursId}")
    public ResponseEntity<?> createModule(@PathVariable Long coursId, @Valid @RequestBody ModuleRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            ModuleResponse module = moduleService.createModule(coursId, request, email);
            return ResponseEntity.ok(module);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            ModuleResponse module = moduleService.updateModule(id, request, email);
            return ResponseEntity.ok(module);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            moduleService.deleteModule(id, email);
            return ResponseEntity.ok(new MessageResponse("Module supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<ModuleResponse>> getModulesByCours(@PathVariable Long coursId) {
        List<ModuleResponse> modules = moduleService.getModulesByCours(coursId);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getModuleById(@PathVariable Long id) {
        try {
            ModuleResponse module = moduleService.getModuleById(id);
            return ResponseEntity.ok(module);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
