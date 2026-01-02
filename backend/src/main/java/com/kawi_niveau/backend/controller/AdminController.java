package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    // Vérifier si l'utilisateur connecté est admin
    private void checkAdminRole(Authentication authentication) {
        User currentUser = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès non autorisé - Rôle administrateur requis");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean archived,
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserAdminResponse> users = adminService.searchUsers(search, role, archived, pageable);
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserAdminResponse>> getAllActiveUsers(Authentication authentication) {
        checkAdminRole(authentication);
        List<UserAdminResponse> users = adminService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserAdminResponse> getUserById(
            @PathVariable Long userId, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        UserAdminResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/change-role")
    public ResponseEntity<?> changeUserRole(
            @RequestBody AdminChangeRoleRequest request, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            UserAdminResponse updatedUser = adminService.changeUserRole(
                    request.getUserId(), 
                    request.getRole()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du changement de rôle: " + e.getMessage()));
        }
    }

    @PutMapping("/users/toggle-status")
    public ResponseEntity<?> toggleUserStatus(
            @RequestBody AdminUserStatusRequest request, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            UserAdminResponse updatedUser = adminService.toggleUserStatus(
                    request.getUserId(), 
                    request.isArchived()
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du changement de statut: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/unlock")
    public ResponseEntity<?> unlockUserAccount(
            @PathVariable Long userId, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            adminService.unlockUserAccount(userId);
            return ResponseEntity.ok(new MessageResponse("Compte utilisateur déverrouillé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du déverrouillage: " + e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest request, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            UserAdminResponse newUser = adminService.createUser(request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la création: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        request.setUserId(userId);
        
        try {
            UserAdminResponse updatedUser = adminService.updateUser(request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(new MessageResponse("Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @PostMapping("/users/bulk-action")
    public ResponseEntity<?> bulkAction(
            @RequestBody BulkActionRequest request, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            adminService.bulkAction(request.getUserIds(), request.getAction(), request.getNewRole());
            return ResponseEntity.ok(new MessageResponse("Action groupée exécutée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de l'action groupée: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<?> resetUserPassword(
            @PathVariable Long userId, 
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        try {
            adminService.resetUserPassword(userId);
            return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la réinitialisation: " + e.getMessage()));
        }
    }

    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsers(Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            List<UserAdminResponse> users = adminService.getAllActiveUsers();
            StringBuilder csv = new StringBuilder();
            
            // Headers
            csv.append("ID,Email,Prenom,Nom,Role,Email Verifie,Statut,Date d'inscription,Telephone\n");
            
            // Data
            for (UserAdminResponse user : users) {
                csv.append(user.getId()).append(",")
                   .append(user.getEmail()).append(",")
                   .append(user.getFirstName()).append(",")
                   .append(user.getLastName()).append(",")
                   .append(user.getRole()).append(",")
                   .append(user.isEmailVerified() ? "Oui" : "Non").append(",")
                   .append(user.isArchived() ? "Desactive" : "Actif").append(",")
                   .append(user.getCreatedAt() != null ? new java.util.Date(user.getCreatedAt()).toString() : "").append(",")
                   .append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                   .append("\n");
            }
            
            byte[] csvBytes = csv.toString().getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=users.csv");
            headers.add("Content-Type", "text/csv; charset=UTF-8");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats/users")
    public ResponseEntity<?> getUserStats(Authentication authentication) {
        checkAdminRole(authentication);
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByArchivedFalse();
        long adminUsers = userRepository.countByRoleAndArchivedFalse(Role.ADMIN);
        long formateurUsers = userRepository.countByRoleAndArchivedFalse(Role.FORMATEUR);
        long etudiantUsers = userRepository.countByRoleAndArchivedFalse(Role.ETUDIANT);
        
        return ResponseEntity.ok(new UserStatsResponse(
                totalUsers, activeUsers, adminUsers, formateurUsers, etudiantUsers
        ));
    }
}