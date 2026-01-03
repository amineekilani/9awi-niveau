package com.kawi_niveau.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ETUDIANT;

    @Column(name = "provider")
    private String provider; // "local" ou "google"

    @Column(name = "provider_id")
    private String providerId; // ID Google de l'utilisateur

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Long resetTokenExpiry;

    @Column(name = "delete_token")
    private String deleteToken;

    @Column(name = "delete_token_expiry")
    private Long deleteTokenExpiry;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_failed_login")
    private Long lastFailedLogin;

    @Column(name = "account_locked_until")
    private Long accountLockedUntil;

    @Column(name = "archived")
    private boolean archived = false;

    @Column(name = "archived_at")
    private Long archivedAt;

    @Column(name = "profile_image")
    private String profileImage; // Nom du fichier image de profil

    @Column(name = "created_at")
    private Long createdAt; // Timestamp de l'inscription

    @Column(name = "phone_number")
    private String phoneNumber; // Format: +216XXXXXXXX (8 chiffres après +216)

    @Column(name = "domaine_specialisation", length = 100)
    private String domaineSpecialisation; // Domaine d'expertise pour les formateurs
}