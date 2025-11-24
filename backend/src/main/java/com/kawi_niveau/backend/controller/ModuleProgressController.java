package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ModuleProgressResponse;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.ModuleProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/module-progress")
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleProgressController {

    @Autowired
    private ModuleProgressService moduleProgressService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<ModuleProgressResponse>> getModulesWithProgress(
            @PathVariable Long coursId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<ModuleProgressResponse> modules = moduleProgressService.getModulesWithProgress(user.getId(), coursId);
        return ResponseEntity.ok(modules);
    }
}
