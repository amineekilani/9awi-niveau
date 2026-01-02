package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.CreateUserRequest;
import com.kawi_niveau.backend.dto.UpdateUserRequest;
import com.kawi_niveau.backend.dto.UserAdminResponse;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<UserAdminResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToAdminResponse);
    }

    public Page<UserAdminResponse> searchUsers(String search, String role, Boolean archived, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                )
            );
        }

        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("role"), Role.valueOf(role))
            );
        }

        if (archived != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("archived"), archived)
            );
        }

        return userRepository.findAll(spec, pageable)
                .map(this::convertToAdminResponse);
    }

    public List<UserAdminResponse> getAllActiveUsers() {
        return userRepository.findByArchivedFalse()
                .stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());
    }

    public UserAdminResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToAdminResponse(user);
    }

    public UserAdminResponse createUser(CreateUserRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmailVerified(request.isEmailVerified());
        user.setCreatedAt(System.currentTimeMillis());
        user.setProvider("local");

        // Générer un mot de passe temporaire
        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));

        try {
            Role role = Role.valueOf(request.getRole());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + request.getRole());
        }

        User savedUser = userRepository.save(user);
        
        // TODO: Envoyer un email avec le mot de passe temporaire
        // emailService.sendTempPassword(user.getEmail(), tempPassword);
        
        return convertToAdminResponse(savedUser);
    }

    public UserAdminResponse updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmailVerified(request.isEmailVerified());

        userRepository.save(user);
        return convertToAdminResponse(user);
    }

    public UserAdminResponse changeUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        try {
            Role role = Role.valueOf(newRole);
            user.setRole(role);
            userRepository.save(user);
            return convertToAdminResponse(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + newRole);
        }
    }

    public UserAdminResponse toggleUserStatus(Long userId, boolean archived) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setArchived(archived);
        if (archived) {
            user.setArchivedAt(System.currentTimeMillis());
        } else {
            user.setArchivedAt(null);
        }
        
        userRepository.save(user);
        return convertToAdminResponse(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Soft delete - marquer comme archivé
        user.setArchived(true);
        user.setArchivedAt(System.currentTimeMillis());
        userRepository.save(user);
    }

    public void bulkAction(List<Long> userIds, String action, String newRole) {
        List<User> users = userRepository.findAllById(userIds);
        
        switch (action.toLowerCase()) {
            case "archive":
                users.forEach(user -> {
                    user.setArchived(true);
                    user.setArchivedAt(System.currentTimeMillis());
                });
                break;
            case "activate":
                users.forEach(user -> {
                    user.setArchived(false);
                    user.setArchivedAt(null);
                });
                break;
            case "change_role":
                if (newRole != null) {
                    Role role = Role.valueOf(newRole);
                    users.forEach(user -> user.setRole(role));
                }
                break;
            case "delete":
                users.forEach(user -> {
                    user.setArchived(true);
                    user.setArchivedAt(System.currentTimeMillis());
                });
                break;
            default:
                throw new RuntimeException("Action non supportée: " + action);
        }
        
        userRepository.saveAll(users);
    }

    public void unlockUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastFailedLogin(null);
        
        userRepository.save(user);
    }

    public void resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        
        userRepository.save(user);
        
        // TODO: Envoyer un email avec le nouveau mot de passe
        // emailService.sendTempPassword(user.getEmail(), tempPassword);
    }

    private String generateTempPassword() {
        return "Temp" + UUID.randomUUID().toString().substring(0, 8) + "!";
    }

    private UserAdminResponse convertToAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.isArchived(),
                user.getCreatedAt(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getFailedLoginAttempts(),
                user.getAccountLockedUntil()
        );
    }
}