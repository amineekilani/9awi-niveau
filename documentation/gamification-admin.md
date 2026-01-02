# Documentation - Système de Gamification Admin

## Vue d'ensemble

Le système de gamification a été implémenté pour motiver les apprenants à travers des badges, défis, points d'expérience (XP), niveaux et classements. Cette documentation couvre la partie administration du système.

> **⚠️ Note importante** : Avant d'utiliser le système de gamification, assurez-vous que les tables de base de données sont correctement créées. Utilisez l'endpoint `/api/init/gamification-tables` ou exécutez le script `fix_gamification_issues.bat` pour initialiser le système.

---

## 0. Initialisation du Système

### 0.1 Prérequis

Avant d'utiliser les fonctionnalités de gamification, vous devez :

1. **Créer les tables de gamification** :

   - Via API : `POST /api/init/gamification-tables`
   - Via script : Exécuter `fix_gamification_issues.bat`
   - Via MySQL : Exécuter `create_gamification_tables_simple.sql`

2. **Corriger les doublons** (si nécessaire) :
   - Via API : `POST /api/init/fix-duplicates`
   - Via MySQL : Exécuter `fix_user_xp_duplicates.sql`

### 0.2 Endpoints d'Initialisation

- `POST /api/init/gamification-tables` - Créer toutes les tables de gamification
- `POST /api/init/fix-duplicates` - Supprimer les doublons dans user_xp

### 0.3 Protection contre les Erreurs

Le système inclut une protection automatique contre les erreurs de gamification :

- Les erreurs de gamification n'interrompent plus la soumission des quiz
- Les erreurs de gamification n'interrompent plus la progression des cours
- Les messages d'erreur sont loggés mais n'affectent pas l'expérience utilisateur

---

## 1. Nouvelles Entités et Relations

### 1.1 Entités Principales

#### Badge

- **Attributs** :
  - `id` : Long (PK)
  - `name` : String (unique, non null)
  - `description` : String (TEXT)
  - `iconUrl` : String
  - `criteriaType` : BadgeCriteriaType (enum)
  - `criteriaValue` : Integer
  - `isActive` : boolean (défaut: true)
  - `createdAt` : Long
  - `updatedAt` : Long

#### UserBadge

- **Attributs** :
  - `id` : Long (PK)
  - `user` : User (FK, non null)
  - `badge` : Badge (FK, non null)
  - `earnedAt` : Long

#### UserXP

- **Attributs** :
  - `id` : Long (PK)
  - `user` : User (FK, unique, non null)
  - `totalXP` : Integer (défaut: 0)
  - `currentLevel` : Integer (défaut: 1)
  - `xpToNextLevel` : Integer (défaut: 100)
  - `lastUpdated` : Long

#### Level

- **Attributs** :
  - `id` : Long (PK)
  - `level` : Integer (unique, non null)
  - `xpRequired` : Integer
  - `name` : String (non null)
  - `description` : String (TEXT)
  - `createdAt` : Long

#### Challenge

- **Attributs** :
  - `id` : Long (PK)
  - `name` : String (non null)
  - `description` : String (TEXT)
  - `challengeType` : ChallengeType (enum)
  - `targetValue` : Integer
  - `xpReward` : Integer
  - `startDate` : Long
  - `endDate` : Long
  - `isActive` : boolean (défaut: true)
  - `createdAt` : Long
  - `updatedAt` : Long

#### UserChallenge

- **Attributs** :
  - `id` : Long (PK)
  - `user` : User (FK, non null)
  - `challenge` : Challenge (FK, non null)
  - `currentProgress` : Integer (défaut: 0)
  - `isCompleted` : boolean (défaut: false)
  - `completedAt` : Long
  - `joinedAt` : Long

### 1.2 Enums

#### BadgeCriteriaType

```java
COURS_COMPLETED,        // Nombre de cours terminés
QUIZ_PASSED,           // Nombre de quiz réussis
PERFECT_SCORE,         // Score parfait sur un quiz
STREAK_DAYS,           // Jours consécutifs de connexion
XP_EARNED,             // Points XP gagnés
FIRST_COURSE,          // Premier cours terminé
FIRST_QUIZ,            // Premier quiz réussi
CHALLENGE_COMPLETED,   // Défi terminé
LEVEL_REACHED          // Niveau atteint
```

#### ChallengeType

```java
COMPLETE_COURSES,      // Terminer X cours
PASS_QUIZZES,         // Réussir X quiz
EARN_XP,              // Gagner X points XP
DAILY_LOGIN,          // Se connecter X jours consécutifs
PERFECT_SCORES,       // Obtenir X scores parfaits
WEEKLY_ACTIVITY,      // Activité hebdomadaire
MONTHLY_GOAL          // Objectif mensuel
```

### 1.3 Relations

#### Relations One-to-Many

- `User` → `UserBadge` (Un utilisateur peut avoir plusieurs badges)
- `Badge` → `UserBadge` (Un badge peut être obtenu par plusieurs utilisateurs)
- `User` → `UserChallenge` (Un utilisateur peut participer à plusieurs défis)
- `Challenge` → `UserChallenge` (Un défi peut avoir plusieurs participants)

#### Relations One-to-One

- `User` → `UserXP` (Un utilisateur a un seul profil XP)

#### Relations Many-to-Many (via tables de liaison)

- `User` ↔ `Badge` (via `UserBadge`)
- `User` ↔ `Challenge` (via `UserChallenge`)

### 1.4 Contraintes et Index

- Index unique sur `UserBadge(user_id, badge_id)`
- Index unique sur `UserChallenge(user_id, challenge_id)`
- Index unique sur `UserXP(user_id)`
- Index unique sur `Level(level)`
- Index unique sur `Badge(name)`

---

## 2. Nouveaux Cas d'Utilisation

### 2.1 Acteur : Administrateur

#### Gestion des Badges

- **UC-GAM-01** : Consulter la liste des badges

  - _Description_ : L'admin peut voir tous les badges avec leurs statistiques
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Affichage de la liste paginée des badges

- **UC-GAM-02** : Créer un nouveau badge

  - _Description_ : L'admin peut créer un badge avec ses critères d'obtention
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Badge créé et disponible pour les utilisateurs

- **UC-GAM-03** : Modifier un badge existant

  - _Description_ : L'admin peut modifier les propriétés d'un badge
  - _Préconditions_ : Badge existant, être connecté en tant qu'admin
  - _Postconditions_ : Badge mis à jour

- **UC-GAM-04** : Supprimer un badge

  - _Description_ : L'admin peut supprimer un badge non utilisé
  - _Préconditions_ : Badge sans utilisateurs, être connecté en tant qu'admin
  - _Postconditions_ : Badge supprimé du système

- **UC-GAM-05** : Activer/Désactiver un badge
  - _Description_ : L'admin peut activer ou désactiver un badge
  - _Préconditions_ : Badge existant, être connecté en tant qu'admin
  - _Postconditions_ : Statut du badge modifié

#### Gestion des Défis

- **UC-GAM-06** : Consulter la liste des défis

  - _Description_ : L'admin peut voir tous les défis avec leurs statistiques
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Affichage de la liste paginée des défis

- **UC-GAM-07** : Créer un nouveau défi

  - _Description_ : L'admin peut créer un défi temporaire avec objectifs
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Défi créé et disponible pour participation

- **UC-GAM-08** : Modifier un défi existant

  - _Description_ : L'admin peut modifier les propriétés d'un défi
  - _Préconditions_ : Défi existant, être connecté en tant qu'admin
  - _Postconditions_ : Défi mis à jour

- **UC-GAM-09** : Supprimer un défi

  - _Description_ : L'admin peut supprimer un défi sans participants
  - _Préconditions_ : Défi sans participants, être connecté en tant qu'admin
  - _Postconditions_ : Défi supprimé du système

- **UC-GAM-10** : Activer/Désactiver un défi
  - _Description_ : L'admin peut activer ou désactiver un défi
  - _Préconditions_ : Défi existant, être connecté en tant qu'admin
  - _Postconditions_ : Statut du défi modifié

#### Consultation des Classements

- **UC-GAM-11** : Consulter le classement général

  - _Description_ : L'admin peut voir le classement des utilisateurs par XP
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Affichage du classement paginé

- **UC-GAM-12** : Consulter le top classement

  - _Description_ : L'admin peut voir le podium des meilleurs utilisateurs
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Affichage du top N utilisateurs

- **UC-GAM-13** : Exporter les données de classement
  - _Description_ : L'admin peut exporter le classement en CSV
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Fichier CSV téléchargé

#### Consultation des Statistiques

- **UC-GAM-14** : Consulter les statistiques de gamification
  - _Description_ : L'admin peut voir les métriques globales du système
  - _Préconditions_ : Être connecté en tant qu'admin
  - _Postconditions_ : Affichage des statistiques générales

### 2.2 Acteur : Système (Automatique)

#### Attribution Automatique

- **UC-GAM-15** : Attribuer des XP automatiquement

  - _Description_ : Le système attribue des XP lors d'actions utilisateur
  - _Préconditions_ : Action déclenchante (cours terminé, quiz réussi, etc.)
  - _Postconditions_ : XP ajoutés au profil utilisateur

- **UC-GAM-16** : Vérifier l'éligibilité aux badges

  - _Description_ : Le système vérifie si un utilisateur mérite un badge
  - _Préconditions_ : Changement dans le profil utilisateur
  - _Postconditions_ : Badge attribué si critères remplis

- **UC-GAM-17** : Calculer les montées de niveau

  - _Description_ : Le système calcule les nouveaux niveaux basés sur les XP
  - _Préconditions_ : Ajout de XP à un utilisateur
  - _Postconditions_ : Niveau mis à jour si seuil atteint

- **UC-GAM-18** : Mettre à jour la progression des défis
  - _Description_ : Le système met à jour la progression des défis actifs
  - _Préconditions_ : Action utilisateur liée à un défi
  - _Postconditions_ : Progression du défi mise à jour

---

## 3. Nouvelles Fonctionnalités

### 3.1 Interface d'Administration

#### Dashboard de Gamification

- **Vue d'ensemble** avec statistiques globales :

  - Nombre total de badges (actifs/inactifs)
  - Nombre total de défis (actifs/expirés)
  - Total des XP distribués
  - Moyenne XP par utilisateur
  - Badges obtenus au total
  - Défis terminés au total

- **Navigation par onglets** :
  - Vue d'ensemble
  - Gestion des badges
  - Gestion des défis
  - Classements

#### Gestion des Badges

- **Liste des badges** avec :

  - Tri par nom, date de création, statut
  - Pagination configurable
  - Recherche et filtres
  - Affichage du nombre d'utilisateurs ayant le badge

- **Création/Modification de badges** :

  - Formulaire avec validation
  - Sélection du type de critère
  - Configuration de la valeur requise
  - Upload d'icône (URL)
  - Activation/désactivation

- **Actions sur les badges** :
  - Modifier les propriétés
  - Activer/désactiver
  - Supprimer (si non utilisé)
  - Voir les statistiques d'utilisation

#### Gestion des Défis

- **Liste des défis** avec :

  - Tri par nom, date, statut
  - Pagination configurable
  - Indicateurs visuels (actif, expiré)
  - Statistiques de participation

- **Création/Modification de défis** :

  - Formulaire avec validation
  - Sélection du type de défi
  - Configuration des dates début/fin
  - Définition de l'objectif et récompense XP
  - Activation/désactivation

- **Suivi des défis** :
  - Nombre de participants
  - Taux de réussite
  - Progression en temps réel
  - Gestion des défis expirés

#### Classements

- **Vue podium** pour le top 3 :

  - Design attractif avec médailles
  - Informations détaillées (XP, niveau, badges)
  - Couleurs par niveau d'utilisateur

- **Classement complet** :
  - Table paginée avec tous les utilisateurs
  - Tri par XP, niveau, badges
  - Indicateurs visuels pour les top positions
  - Export CSV des données

### 3.2 API REST

#### Endpoints d'Initialisation et Maintenance

- `POST /api/init/gamification-tables` - Créer les tables de gamification
- `POST /api/init/fix-duplicates` - Corriger les doublons dans user_xp

#### Endpoints Badges

- `GET /api/admin/gamification/badges` - Liste paginée des badges
- `GET /api/admin/gamification/badges/active` - Badges actifs
- `GET /api/admin/gamification/badges/{id}` - Détails d'un badge
- `POST /api/admin/gamification/badges` - Créer un badge
- `PUT /api/admin/gamification/badges/{id}` - Modifier un badge
- `DELETE /api/admin/gamification/badges/{id}` - Supprimer un badge
- `PUT /api/admin/gamification/badges/{id}/toggle-status` - Changer le statut

#### Endpoints Défis

- `GET /api/admin/gamification/challenges` - Liste paginée des défis
- `GET /api/admin/gamification/challenges/active` - Défis actifs
- `GET /api/admin/gamification/challenges/{id}` - Détails d'un défi
- `POST /api/admin/gamification/challenges` - Créer un défi
- `PUT /api/admin/gamification/challenges/{id}` - Modifier un défi
- `DELETE /api/admin/gamification/challenges/{id}` - Supprimer un défi
- `PUT /api/admin/gamification/challenges/{id}/toggle-status` - Changer le statut

#### Endpoints Classements

- `GET /api/admin/gamification/leaderboard` - Classement paginé
- `GET /api/admin/gamification/leaderboard/top/{limit}` - Top N utilisateurs
- `GET /api/admin/gamification/leaderboard/export` - Export CSV du classement

#### Endpoints Statistiques

- `GET /api/admin/gamification/stats` - Statistiques globales

### 3.3 Services Backend

#### GamificationService

- **Attribution automatique d'XP** lors d'événements
- **Vérification des badges** basée sur les critères
- **Calcul des niveaux** automatique
- **Gestion des événements** d'apprentissage
- **Protection contre les erreurs** : Les erreurs de gamification sont capturées et loggées sans interrompre les fonctionnalités principales

#### Services spécialisés

- **BadgeService** : CRUD complet des badges
- **ChallengeService** : CRUD complet des défis
- **LeaderboardService** : Génération des classements
- **Repositories optimisés** avec requêtes personnalisées et gestion des doublons

### 3.4 Base de Données

#### Tables créées

- `badges` - Stockage des badges
- `user_badges` - Attribution des badges aux utilisateurs
- `user_xp` - Profils XP des utilisateurs
- `levels` - Définition des niveaux
- `challenges` - Stockage des défis
- `user_challenges` - Participation aux défis

#### Données par défaut

- **10 niveaux prédéfinis** (Débutant à Grand Maître)
- **10 badges de base** couvrant les principales actions
- **Index optimisés** pour les performances
- **Contraintes d'intégrité** pour la cohérence

#### Intégration avec l'existant

- **Attribution automatique d'XP** lors de la complétion de cours
- **Déclenchement des badges** lors de la réussite de quiz
- **Calcul des statistiques** en temps réel

### 3.5 Interface Utilisateur

#### Composants Frontend Implémentés

- **BadgeManagementComponent** : Gestion complète des badges
- **ChallengeManagementComponent** : Gestion complète des défis
- **LeaderboardManagementComponent** : Affichage des classements avec export CSV

#### Design moderne

- **Interface responsive** adaptée mobile/desktop
- **Composants réutilisables** Angular standalone
- **Animations et transitions** fluides
- **Thème cohérent** avec l'application existante

#### Expérience utilisateur

- **Messages de feedback** en temps réel
- **Validation des formulaires** côté client
- **États de chargement** avec spinners
- **Confirmations** pour les actions critiques
- **Export de données** en CSV

---

## 4. Architecture Technique

### 4.1 Backend (Spring Boot)

- **Entités JPA** avec relations optimisées
- **Repositories** avec requêtes personnalisées
- **Services** avec logique métier encapsulée
- **Contrôleurs REST** avec sécurité admin
- **DTOs** pour les échanges API

### 4.2 Frontend (Angular)

- **Composants standalone** modulaires
- **Services** pour les appels API
- **Interfaces TypeScript** typées
- **Routing** pour la navigation
- **Formulaires réactifs** avec validation

### 4.3 Sécurité et Robustesse

- **Authentification** requise pour tous les endpoints
- **Autorisation** limitée aux administrateurs
- **Validation** des données côté serveur
- **Protection CSRF** activée
- **Gestion des erreurs** : Protection automatique contre les erreurs de gamification
- **Gestion des doublons** : Repository UserXP modifié pour gérer les doublons automatiquement

---

## 6. Dépannage et Maintenance

### 6.1 Problèmes Courants

#### Erreur "Query did not return a unique result"

- **Cause** : Doublons dans la table user_xp
- **Solution** : Appeler `POST /api/init/fix-duplicates` ou exécuter `fix_user_xp_duplicates.sql`

#### Erreur lors de la soumission de quiz

- **Cause** : Tables de gamification manquantes
- **Solution** : Appeler `POST /api/init/gamification-tables` ou exécuter `create_gamification_tables_simple.sql`

#### Fonctionnalités de gamification non disponibles

- **Cause** : Tables non initialisées
- **Solution** : Exécuter le script `fix_gamification_issues.bat` ou les endpoints d'initialisation

### 6.2 Scripts de Maintenance

- `fix_gamification_issues.bat` - Script complet de correction
- `create_gamification_tables_simple.sql` - Création manuelle des tables
- `fix_user_xp_duplicates.sql` - Suppression des doublons

### 6.3 Vérification du Système

Pour vérifier que le système fonctionne correctement :

1. Vérifier que les tables existent : `SHOW TABLES LIKE '%gamification%'`
2. Vérifier l'absence de doublons : `SELECT user_id, COUNT(*) FROM user_xp GROUP BY user_id HAVING COUNT(*) > 1`
3. Tester la soumission d'un quiz
4. Vérifier les logs pour les erreurs de gamification

---

## 7. Points d'Extension Futurs

### 7.1 Fonctionnalités Apprenants

- Interface apprenant pour voir ses badges/XP
- Participation aux défis
- Classements personnels
- Notifications de récompenses

### 7.2 Gamification Avancée

- Badges conditionnels complexes
- Défis collaboratifs
- Saisons de compétition
- Récompenses physiques

### 7.3 Analytics

- Tableaux de bord détaillés
- Métriques d'engagement
- Rapports d'utilisation
- A/B testing des récompenses

---

_Documentation mise à jour le : 2 janvier 2026_
_Version : 1.1 - Inclut les corrections de robustesse et d'initialisation_
_Auteur : Système de Gamification Kawi Niveau_
