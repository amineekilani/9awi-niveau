# Documentation - Gestion des Utilisateurs et Dashboard Admin

## Vue d'ensemble

Cette documentation couvre les nouvelles fonctionnalités développées pour la gestion des utilisateurs et le dashboard administrateur de la plateforme 9awi Niveau.

---

## 1. Nouvelles Entités et Relations

### 1.1 Entités Modifiées

#### User (Entité existante - Modifiée)

```java
@Entity
@Table(name = "users")
public class User {
    // Champs existants...

    // Nouveaux champs pour l'administration
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ETUDIANT; // Modifié pour inclure ADMIN

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
}
```

#### Role (Entité existante - Modifiée)

```java
public enum Role {
    ETUDIANT,
    FORMATEUR,
    ADMIN  // Nouveau rôle ajouté
}
```

### 1.2 Nouvelles Entités DTO

#### UserAdminResponse

```java
public class UserAdminResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean emailVerified;
    private boolean archived;
    private Long createdAt;
    private String phoneNumber;
    private Integer failedLoginAttempts;
    private Long accountLockedUntil;
}
```

#### UserStatsResponse

```java
public class UserStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long adminUsers;
    private long formateurUsers;
    private long etudiantUsers;
}
```

#### CreateUserRequest

```java
public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String phoneNumber;
    private String dateOfBirth;
    private boolean emailVerified = true;
}
```

#### UpdateUserRequest

```java
public class UpdateUserRequest {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;
    private boolean emailVerified;
}
```

#### BulkActionRequest

```java
public class BulkActionRequest {
    private List<Long> userIds;
    private String action; // "archive", "activate", "delete", "change_role"
    private String newRole;
}
```

#### AdminChangeRoleRequest

```java
public class AdminChangeRoleRequest {
    private Long userId;
    private String role;
}
```

#### AdminUserStatusRequest

```java
public class AdminUserStatusRequest {
    private Long userId;
    private boolean archived;
}
```

### 1.3 Relations entre Entités

```
User (1) ←→ (1) Role [Énumération]
├── Un utilisateur a un rôle unique
├── Les rôles possibles : ETUDIANT, FORMATEUR, ADMIN
└── Relation gérée par @Enumerated

AdminService (Service) ←→ (*) User
├── Gère les opérations CRUD sur les utilisateurs
├── Gère les statistiques utilisateurs
└── Gère les actions groupées

UserRepository (Repository) ←→ (*) User
├── Méthodes de recherche par rôle
├── Méthodes de comptage par statut
└── Méthodes de filtrage avancé
```

---

## 2. Nouveaux Cas d'Utilisation

### 2.1 Acteur : Administrateur

#### CU-ADMIN-001 : Se connecter au dashboard admin

- **Acteur principal** : Administrateur
- **Préconditions** : L'utilisateur a le rôle ADMIN
- **Scénario principal** :
  1. L'administrateur accède à l'URL /admin
  2. Le système vérifie les permissions
  3. Le système affiche le dashboard admin avec sidebar
  4. L'administrateur peut naviguer entre les sections

#### CU-ADMIN-002 : Consulter les statistiques utilisateurs

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur accède au dashboard
  2. Le système affiche les statistiques en temps réel
  3. L'administrateur peut actualiser les données
  4. Le système affiche : total, actifs, par rôle

#### CU-ADMIN-003 : Lister les utilisateurs

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur accède à la section utilisateurs
  2. Le système affiche la liste paginée des utilisateurs
  3. L'administrateur peut filtrer par rôle, statut
  4. L'administrateur peut rechercher par nom/email
  5. L'administrateur peut trier les colonnes

#### CU-ADMIN-004 : Créer un utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur "Nouvel utilisateur"
  2. Le système affiche le formulaire de création
  3. L'administrateur saisit les informations
  4. Le système valide les données
  5. Le système crée l'utilisateur avec mot de passe temporaire
  6. Le système confirme la création

#### CU-ADMIN-005 : Modifier un utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur "Modifier" pour un utilisateur
  2. Le système affiche le formulaire pré-rempli
  3. L'administrateur modifie les informations
  4. Le système valide et sauvegarde
  5. Le système confirme la modification

#### CU-ADMIN-006 : Changer le rôle d'un utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur sélectionne un nouveau rôle
  2. Le système demande confirmation
  3. L'administrateur confirme
  4. Le système met à jour le rôle
  5. Le système confirme le changement

#### CU-ADMIN-007 : Activer/Désactiver un utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur activer/désactiver
  2. Le système demande confirmation
  3. L'administrateur confirme
  4. Le système change le statut archived
  5. Le système confirme l'action

#### CU-ADMIN-008 : Supprimer un utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur supprimer
  2. Le système demande confirmation
  3. L'administrateur confirme
  4. Le système effectue une suppression logique
  5. Le système confirme la suppression

#### CU-ADMIN-009 : Actions groupées sur utilisateurs

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur sélectionne plusieurs utilisateurs
  2. L'administrateur choisit une action groupée
  3. Le système demande confirmation
  4. L'administrateur confirme
  5. Le système applique l'action à tous les utilisateurs sélectionnés

#### CU-ADMIN-010 : Déverrouiller un compte utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin, compte verrouillé
- **Scénario principal** :
  1. L'administrateur identifie un compte verrouillé
  2. L'administrateur clique sur déverrouiller
  3. Le système demande confirmation
  4. Le système remet à zéro les tentatives échouées
  5. Le système confirme le déverrouillage

#### CU-ADMIN-011 : Réinitialiser mot de passe utilisateur

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur réinitialiser mot de passe
  2. Le système demande confirmation
  3. Le système génère un mot de passe temporaire
  4. Le système met à jour le mot de passe
  5. Le système confirme la réinitialisation

#### CU-ADMIN-012 : Exporter les données utilisateurs

- **Acteur principal** : Administrateur
- **Préconditions** : Connecté en tant qu'admin
- **Scénario principal** :
  1. L'administrateur clique sur exporter
  2. Le système génère un fichier CSV
  3. Le système télécharge le fichier
  4. Le fichier contient toutes les données utilisateurs

---

## 3. Nouvelles Fonctionnalités

### 3.1 Interface d'Administration

#### 3.1.1 Layout Admin avec Navigation Latérale

- **Description** : Interface moderne avec sidebar collapsible
- **Composants** :
  - Logo et branding 9awi Niveau
  - Menu de navigation avec icônes
  - Informations utilisateur connecté
  - Actions rapides (retour app, déconnexion)
  - Design responsive pour mobile

#### 3.1.2 Dashboard Overview

- **Description** : Vue d'ensemble avec statistiques et actions rapides
- **Fonctionnalités** :
  - Statistiques utilisateurs en temps réel
  - Cartes de statistiques colorées
  - Actions rapides vers les sections
  - Activités récentes (placeholder)
  - Bouton d'actualisation des données

#### 3.1.3 Gestion Complète des Utilisateurs

- **Description** : Interface CRUD complète pour les utilisateurs
- **Fonctionnalités** :
  - Liste paginée et triable
  - Recherche en temps réel
  - Filtres par rôle et statut
  - Création d'utilisateurs avec modal
  - Modification d'utilisateurs
  - Changement de rôles en temps réel
  - Activation/désactivation de comptes
  - Suppression logique
  - Déverrouillage de comptes
  - Réinitialisation de mots de passe

### 3.2 Fonctionnalités Backend

#### 3.2.1 Service d'Administration (AdminService)

- **Méthodes principales** :
  - `getAllUsers()` : Liste paginée avec filtres
  - `searchUsers()` : Recherche avancée avec critères
  - `createUser()` : Création avec validation
  - `updateUser()` : Modification des informations
  - `changeUserRole()` : Changement de rôle
  - `toggleUserStatus()` : Activation/désactivation
  - `deleteUser()` : Suppression logique
  - `bulkAction()` : Actions groupées
  - `unlockUserAccount()` : Déverrouillage
  - `resetUserPassword()` : Réinitialisation mot de passe

#### 3.2.2 Contrôleur d'Administration (AdminController)

- **Endpoints sécurisés** :
  - `GET /api/admin/users` : Liste avec pagination et filtres
  - `POST /api/admin/users` : Création d'utilisateur
  - `PUT /api/admin/users/{id}` : Modification d'utilisateur
  - `DELETE /api/admin/users/{id}` : Suppression d'utilisateur
  - `PUT /api/admin/users/change-role` : Changement de rôle
  - `PUT /api/admin/users/toggle-status` : Changement de statut
  - `PUT /api/admin/users/{id}/unlock` : Déverrouillage
  - `PUT /api/admin/users/{id}/reset-password` : Réinitialisation
  - `POST /api/admin/users/bulk-action` : Actions groupées
  - `GET /api/admin/users/export` : Export CSV
  - `GET /api/admin/stats/users` : Statistiques

#### 3.2.3 Sécurité et Permissions

- **Authentification** : JWT avec rôle ADMIN requis
- **Autorisation** : `@PreAuthorize("hasRole('ADMIN')")` sur tous les endpoints
- **Validation** : Vérification double du rôle admin
- **Guards** : Protection des routes frontend avec adminGuard

### 3.3 Fonctionnalités Frontend

#### 3.3.1 Services Angular

- **AdminService** : Service pour les appels API admin
- **AuthService** : Méthode `isAdmin()` ajoutée
- **AdminGuard** : Guard pour protéger les routes admin

#### 3.3.2 Composants Angular

- **AdminLayoutComponent** : Layout principal avec sidebar
- **AdminDashboardOverviewComponent** : Dashboard avec statistiques
- **AdminUsersComponent** : Gestion complète des utilisateurs
- **UserModalComponent** : Modal pour création/édition

#### 3.3.3 Fonctionnalités UX/UI

- **Design moderne** : Interface cohérente avec l'application
- **Responsive** : Adaptation mobile et tablette
- **Feedback utilisateur** : Messages de succès/erreur
- **Loading states** : Indicateurs de chargement
- **Confirmations** : Dialogues de confirmation pour actions critiques
- **Pagination** : Navigation efficace dans les listes
- **Tri et filtres** : Outils de recherche avancés

### 3.4 Actions Groupées (Bulk Actions)

#### 3.4.1 Sélection Multiple

- Cases à cocher individuelles et globale
- Compteur d'éléments sélectionnés
- Interface contextuelle pour actions groupées

#### 3.4.2 Actions Disponibles

- **Activation en masse** : Activer plusieurs comptes
- **Désactivation en masse** : Désactiver plusieurs comptes
- **Changement de rôle en masse** : Modifier le rôle de plusieurs utilisateurs
- **Suppression en masse** : Supprimer plusieurs utilisateurs

### 3.5 Export et Reporting

#### 3.5.1 Export CSV

- Export de toutes les données utilisateurs
- Format CSV avec headers français
- Téléchargement automatique
- Nom de fichier avec date

#### 3.5.2 Statistiques en Temps Réel

- Total des utilisateurs
- Utilisateurs actifs/inactifs
- Répartition par rôles
- Mise à jour automatique après actions

---

## 4. Architecture Technique

### 4.1 Structure Backend

```
backend/
├── controller/
│   └── AdminController.java
├── service/
│   └── AdminService.java
├── dto/
│   ├── UserAdminResponse.java
│   ├── UserStatsResponse.java
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── BulkActionRequest.java
│   ├── AdminChangeRoleRequest.java
│   └── AdminUserStatusRequest.java
├── entity/
│   ├── User.java (modifié)
│   └── Role.java (modifié)
└── repository/
    └── UserRepository.java (étendu)
```

### 4.2 Structure Frontend

```
frontend/src/app/
├── admin-layout/
│   ├── admin-layout.ts
│   ├── admin-layout.html
│   └── admin-layout.css
├── admin-dashboard-overview/
│   ├── admin-dashboard-overview.ts
│   ├── admin-dashboard-overview.html
│   └── admin-dashboard-overview.css
├── admin-users/
│   ├── admin-users.ts
│   ├── admin-users.html
│   └── admin-users.css
├── user-modal/
│   ├── user-modal.ts
│   ├── user-modal.html
│   └── user-modal.css
├── admin.service.ts
└── admin-guard.ts
```

### 4.3 Routes et Navigation

```
/admin (AdminLayoutComponent)
├── /admin/dashboard (AdminDashboardOverviewComponent)
├── /admin/users (AdminUsersComponent)
└── /admin/* (Routes futures pour gamification, rapports, etc.)
```

---

## 5. Sécurité et Bonnes Pratiques

### 5.1 Sécurité Backend

- Authentification JWT obligatoire
- Vérification du rôle ADMIN sur tous les endpoints
- Validation des données d'entrée
- Suppression logique (soft delete) au lieu de suppression physique
- Génération sécurisée de mots de passe temporaires

### 5.2 Sécurité Frontend

- Guards pour protéger les routes admin
- Vérification du rôle côté client
- Confirmation utilisateur pour actions critiques
- Gestion des erreurs et timeouts

### 5.3 Bonnes Pratiques

- Code modulaire et réutilisable
- Séparation des responsabilités
- Gestion d'état cohérente
- Interface utilisateur intuitive
- Messages d'erreur explicites
- Documentation complète du code

---

## 6. Migration et Déploiement

### 6.1 Scripts de Migration

- `migration_add_admin_user.sql` : Création du premier utilisateur admin
- Mise à jour de l'énumération Role pour inclure ADMIN
- Ajout des nouveaux champs à la table users

### 6.2 Configuration

- Activation de la sécurité basée sur les méthodes
- Configuration des CORS pour les nouveaux endpoints
- Mise à jour des routes Angular

---

## 7. Tests et Validation

### 7.1 Tests Fonctionnels

- Création, modification, suppression d'utilisateurs
- Changement de rôles et statuts
- Actions groupées
- Export de données
- Sécurité et permissions

### 7.2 Tests d'Interface

- Responsive design sur différents écrans
- Navigation et ergonomie
- Feedback utilisateur
- Performance et chargement

---

Cette documentation couvre l'ensemble des développements réalisés pour la gestion des utilisateurs et le dashboard administrateur. Elle peut être utilisée pour mettre à jour vos diagrammes de classes et de cas d'utilisation.
