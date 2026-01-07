# 🏗️ Architecture Backend - 9awi Niveau

## 📋 Vue d'ensemble

Le backend de **9awi Niveau** est une application Spring Boot 3.5.7 construite avec Java 17, suivant une architecture en couches (layered architecture) et utilisant MySQL 8.0 comme base de données. L'application est conteneurisée avec Docker et expose une API REST pour le frontend Angular.

## 🎯 Technologies Principales

### Framework & Langage

- **Spring Boot 3.5.7** - Framework principal
- **Java 17** - Langage de programmation
- **Maven** - Gestionnaire de dépendances
- **Lombok** - Réduction du code boilerplate

### Base de Données

- **MySQL 8.0** - Base de données relationnelle
- **Spring Data JPA** - ORM et accès aux données
- **Hibernate** - Implémentation JPA

### Sécurité & Authentification

- **Spring Security** - Sécurisation de l'application
- **JWT (JSON Web Tokens)** - Authentification stateless
- **OAuth2 Client** - Authentification Google
- **JJWT 0.11.5** - Gestion des tokens JWT

### Services Externes

- **Brevo (Sendinblue) API 6.0.0** - Envoi d'emails transactionnels
- **Apache PDFBox 2.0.29** - Génération de certificats PDF
- **OpenRouter AI** - Chatbot intelligent (Llama 3.2)

### Recommandations IA

- **Python 3.8+** - Moteur de recommandation
- **Pandas, NumPy, Scikit-learn** - Machine Learning
- **SVD (Singular Value Decomposition)** - Filtrage collaboratif

---

## 📁 Structure du Projet

```
backend/
├── src/main/java/com/kawi_niveau/backend/
│   ├── aspect/              # Programmation orientée aspect (AOP)
│   ├── config/              # Configuration de l'application
│   ├── controller/          # Contrôleurs REST (API endpoints)
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # Entités JPA (modèle de données)
│   ├── event/               # Événements applicatifs
│   ├── exception/           # Gestion des exceptions
│   ├── listener/            # Écouteurs d'événements
│   ├── repository/          # Repositories JPA
│   ├── security/            # Configuration sécurité & JWT
│   ├── service/             # Logique métier
│   └── BackendApplication.java
├── src/main/python/         # Moteur de recommandation IA
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql
│   └── schema.sql
├── src/test/                # Tests unitaires et d'intégration
├── uploads/                 # Fichiers uploadés (images, documents)
├── certificates/            # Certificats PDF générés
├── *.sql                    # Scripts de migration et tests
├── Dockerfile
└── pom.xml
```

---

## 🧩 Architecture en Couches

### 1️⃣ Couche Présentation (Controllers)

**Rôle** : Exposer les endpoints REST et gérer les requêtes HTTP

**Fichiers** : `controller/*.java` (43 contrôleurs)

**Contrôleurs Principaux** :

- `AuthController` - Authentification (login, register, OAuth2, reset password)
- `CoursController` - Gestion des cours
- `ModuleController` - Gestion des modules
- `LeconController` - Gestion des leçons
- `QuizController` - Gestion des quiz
- `ExerciceController` - Gestion des exercices interactifs
- `ParcoursController` - Parcours d'apprentissage personnalisés
- `UserGamificationController` - Gamification (XP, niveaux, badges)
- `AdminController` - Administration des utilisateurs
- `ChatbotController` - Assistant IA
- `CertificateController` - Génération de certificats
- `RecommendationController` - Recommandations personnalisées

**Exemple de structure** :

```java
@RestController
@RequestMapping("/api/cours")
public class CoursController {
    @GetMapping
    public ResponseEntity<List<CoursResponse>> getAllCours() { }

    @PostMapping
    public ResponseEntity<CoursResponse> createCours(@RequestBody CoursRequest request) { }
}
```

### 2️⃣ Couche Service (Business Logic)

**Rôle** : Implémenter la logique métier et orchestrer les opérations

**Fichiers** : `service/*.java` (50+ services)

**Services Clés** :

**Authentification & Utilisateurs**

- `UserDetailsServiceImpl` - Chargement des utilisateurs pour Spring Security
- `OAuth2Service` - Authentification Google
- `EmailService` - Envoi d'emails (vérification, reset password)
- `AdminService` - Gestion administrative des utilisateurs

**Contenu Pédagogique**

- `CoursService` - CRUD cours
- `ModuleService` - CRUD modules
- `LeconService` - CRUD leçons
- `QuizService` - CRUD quiz
- `ExerciceService` - CRUD exercices interactifs
- `EnrollmentService` - Inscriptions aux cours

**Gamification**

- `GamificationService` - Système XP, niveaux, badges
- `BadgeService` - Gestion des badges
- `ChallengeService` - Défis quotidiens/hebdomadaires
- `LeaderboardService` - Classements
- `LevelService` - Gestion des niveaux
- `XPSynchronizationService` - Synchronisation des XP

**Parcours d'Apprentissage**

- `ParcoursService` - CRUD parcours
- `ParcoursProgressionService` - Suivi de progression
- `ParcoursCompletionService` - Validation de complétion
- `ParcoursNotificationService` - Notifications de progression

**Recommandations**

- `RecommendationService` - Génération de recommandations
- `AIRecommendationService` - Recommandations IA (Python)
- `RecommendationUpdateService` - Mise à jour asynchrone
- `RecommendationTriggerService` - Déclenchement automatique

**Autres Services**

- `ChatbotService` - Assistant IA conversationnel
- `CertificateService` - Génération de certificats
- `PdfCertificateService` - Génération PDF
- `ImageUploadService` - Upload d'images
- `FormateurService` - Statistiques formateurs

### 3️⃣ Couche Repository (Accès aux Données)

**Rôle** : Abstraction de l'accès à la base de données via Spring Data JPA

**Fichiers** : `repository/*.java` (28 repositories)

**Repositories Principaux** :

- `UserRepository` - Utilisateurs
- `CoursRepository` - Cours
- `ModuleRepository` - Modules
- `LeconRepository` - Leçons
- `QuizRepository` - Quiz
- `ExerciceRepository` - Exercices
- `EnrollmentRepository` - Inscriptions
- `ParcoursRepository` - Parcours
- `BadgeRepository` - Badges
- `ChallengeRepository` - Défis
- `UserXPRepository` - XP utilisateurs
- `LevelRepository` - Niveaux

**Exemple** :

```java
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByFormateurId(Long formateurId);
    List<Cours> findByCategorie(String categorie);

    @Query("SELECT c FROM Cours c WHERE c.archived = false")
    List<Cours> findAllActive();
}
```

### 4️⃣ Couche Entités (Modèle de Données)

**Rôle** : Représentation des tables de la base de données

**Fichiers** : `entity/*.java` (38 entités)

**Entités Principales** :

**Utilisateurs & Authentification**

- `User` - Utilisateur (apprenant/formateur/admin)
- `Role` - Rôles (ROLE_APPRENANT, ROLE_FORMATEUR, ROLE_ADMIN)
- `UserLogin` - Historique des connexions
- `UserPreferences` - Préférences utilisateur

**Contenu Pédagogique**

- `Cours` - Cours
- `Module` - Modules d'un cours
- `Lecon` - Leçons d'un module
- `Quiz` - Quiz d'évaluation
- `Question` - Questions de quiz
- `Exercice` - Exercices interactifs
- `ExerciceElement` - Éléments d'exercice
- `NiveauDifficulte` - Niveaux de difficulté

**Progression & Résultats**

- `Enrollment` - Inscription à un cours
- `LeconCompletion` - Complétion de leçon
- `ResultatQuiz` - Résultats de quiz
- `ResultatExercice` - Résultats d'exercice

**Gamification**

- `UserXP` - XP et niveau utilisateur
- `Level` - Définition des niveaux
- `Badge` - Badges disponibles
- `UserBadge` - Badges obtenus
- `Challenge` - Défis
- `UserChallenge` - Progression des défis
- `LevelNotification` - Notifications de niveau
- `BadgeNotification` - Notifications de badge
- `ChallengeNotification` - Notifications de défi

**Parcours d'Apprentissage**

- `ParcoursApprentissage` - Parcours
- `ParcoursEtape` - Étapes d'un parcours
- `ParcoursInscription` - Inscription à un parcours
- `ParcoursCondition` - Conditions de déblocage
- `ParcoursNotification` - Notifications de parcours

**Enums**

- `TypeContenu` - COURS, MODULE, LECON, QUIZ
- `TypeParcours` - DEBUTANT, INTERMEDIAIRE, AVANCE, SPECIALISATION
- `TypeCondition` - COURS_COMPLETE, NIVEAU_ATTEINT, BADGE_OBTENU
- `BadgeCriteriaType` - FIRST_COURSE, QUIZ_MASTER, STREAK_7, etc.
- `ChallengeType` - DAILY, WEEKLY, MONTHLY

### 5️⃣ Couche DTO (Data Transfer Objects)

**Rôle** : Objets de transfert entre le frontend et le backend

**Fichiers** : `dto/*.java` (80+ DTOs)

**Catégories de DTOs** :

- **Request** : Données envoyées par le client (LoginRequest, RegisterRequest, CoursRequest)
- **Response** : Données retournées au client (JwtResponse, CoursResponse, UserStatsResponse)
- **Stats** : Statistiques agrégées (GamificationStatsResponse, FormateurStatsResponse)

---

## 🔐 Sécurité & Authentification

### Configuration Sécurité (`security/`)

**SecurityConfig.java**

- Configuration Spring Security
- Endpoints publics vs protégés
- CORS configuration
- Session management (stateless)

**JwtUtils.java**

- Génération de tokens JWT
- Validation des tokens
- Extraction des claims (userId, email, role)

**JwtAuthenticationFilter.java**

- Filtre HTTP pour valider les tokens
- Extraction du token depuis le header `Authorization: Bearer <token>`
- Chargement de l'utilisateur authentifié

**UserDetailsServiceImpl.java**

- Implémentation de `UserDetailsService`
- Chargement des utilisateurs depuis la base de données

### Flux d'Authentification

1. **Login classique** : Email + Password → JWT Token
2. **OAuth2 Google** : Redirection Google → Callback → JWT Token
3. **Requêtes protégées** : Header `Authorization: Bearer <token>`
4. **Refresh** : Token expiré (24h) → Re-login

---

## 🎮 Système de Gamification

### Architecture Gamification

**Composants** :

- **XP (Experience Points)** : Gagnés par complétion de contenu
- **Niveaux** : 10 niveaux (Débutant → Expert)
- **Badges** : Récompenses pour accomplissements
- **Défis** : Objectifs quotidiens/hebdomadaires
- **Leaderboard** : Classement des apprenants

**Services** :

- `GamificationService` - Orchestration générale
- `BadgeService` - Attribution automatique des badges
- `ChallengeService` - Gestion des défis
- `LeaderboardService` - Calcul des classements
- `LevelNotificationService` - Notifications de montée de niveau

**Règles XP** :

- Complétion leçon : 10 XP
- Quiz réussi (>50%) : 20 XP
- Exercice réussi : 15 XP
- Cours complété : 50 XP
- Connexion quotidienne : 5 XP

**Badges Automatiques** :

- `FIRST_COURSE` - Premier cours complété
- `QUIZ_MASTER` - 10 quiz réussis
- `STREAK_7` - 7 jours consécutifs
- `LEVEL_5` - Niveau 5 atteint
- `CHALLENGE_COMPLETE` - Défi complété

---

## 🎯 Système de Parcours d'Apprentissage

### Architecture Parcours

**Entités** :

- `ParcoursApprentissage` - Définition du parcours
- `ParcoursEtape` - Étapes séquentielles
- `ParcoursInscription` - Inscription utilisateur
- `ParcoursCondition` - Conditions de déblocage

**Services** :

- `ParcoursService` - CRUD parcours
- `ParcoursProgressionService` - Calcul de progression
- `ParcoursCompletionService` - Validation de complétion
- `ParcoursValidationService` - Validation des conditions
- `ParcoursIntegrationService` - Intégration avec gamification

**Listeners** :

- `ParcoursProgressionListener` - Écoute les événements de progression
- Déclenche automatiquement les notifications
- Met à jour les statistiques

**Types de Parcours** :

- `DEBUTANT` - Parcours d'initiation
- `INTERMEDIAIRE` - Parcours de perfectionnement
- `AVANCE` - Parcours expert
- `SPECIALISATION` - Parcours thématique

---

## 🤖 Système de Recommandations IA

### Architecture Hybride

**Composants Java** :

- `RecommendationService` - Service principal
- `AIRecommendationService` - Interface avec Python
- `RecommendationUpdateService` - Mise à jour asynchrone
- `RecommendationTriggerService` - Déclenchement automatique

**Composants Python** (`src/main/python/`)

- `recommendation_engine.py` - Moteur ML principal
- `test_recommendations.py` - Tests et validation
- `requirements.txt` - Dépendances Python

### Algorithmes de Recommandation

**1. Filtrage Collaboratif (40%)**

- Utilise SVD (Singular Value Decomposition)
- Identifie des utilisateurs similaires
- Recommande des contenus appréciés par des profils similaires

**2. Filtrage Basé sur le Contenu (40%)**

- Analyse les caractéristiques des cours
- Calcule la similarité cosinus
- Recommande des cours similaires

**3. Recommandations par Niveau (20%)**

- Adapte au niveau actuel (1-10)
- Respecte la progression pédagogique
- Évite les contenus inadaptés

### Règles Pédagogiques

- ❌ Jamais de contenu déjà complété avec succès
- 📉 Score < 50% → contenu de révision
- 📈 Score > 80% → contenu plus avancé
- 🧩 Respect de la hiérarchie : Cours → Module → Leçon → Quiz
- 🎯 Priorité à la motivation et progression

### Déclenchement Automatique

**Événements déclencheurs** :

- Complétion d'un cours
- Réussite d'un quiz
- Montée de niveau
- Connexion quotidienne

**Aspect AOP** :

- `RecommendationUpdateAspect` - Intercepte les événements
- Déclenche la mise à jour asynchrone
- Évite les calculs bloquants

---

## 📧 Système d'Emails

### EmailService

**Fonctionnalités** :

- Envoi d'emails transactionnels via Brevo (Sendinblue)
- Templates HTML personnalisés
- Gestion des erreurs et retry

**Types d'emails** :

- Vérification d'email (inscription)
- Réinitialisation de mot de passe
- Notifications de progression
- Certificats de complétion

**Configuration** :

```properties
brevo.api.key=xkeysib-...
brevo.sender.email=aminekilani901@gmail.com
brevo.sender.name=9awi Niveau
```

---

## 📜 Système de Certificats

### CertificateService & PdfCertificateService

**Fonctionnalités** :

- Génération automatique de certificats PDF
- Personnalisation avec nom, cours, date
- Stockage dans `./certificates/`
- Téléchargement via API

**Technologie** :

- Apache PDFBox 2.0.29
- Génération programmatique de PDF
- Ajout de texte, images, signatures

**Endpoints** :

```
GET /api/certificates/cours/{coursId}
GET /api/certificates/download/{certificateId}
```

---

## 💬 Chatbot IA

### ChatbotService

**Fonctionnalités** :

- Assistant conversationnel intelligent
- Réponses contextuelles sur les cours
- Aide à la navigation
- Suggestions personnalisées

**Technologie** :

- OpenRouter AI API
- Modèle : Meta Llama 3.2 3B Instruct (gratuit)
- Contexte utilisateur (niveau, cours suivis)

**Configuration** :

```properties
ai.api.key=sk-or-v1-...
ai.api.url=https://openrouter.ai/api/v1/chat/completions
ai.model=meta-llama/llama-3.2-3b-instruct:free
```

---

## 🗄️ Base de Données

### Structure

**Tables Principales** (30+ tables) :

- `users` - Utilisateurs
- `cours` - Cours
- `modules` - Modules
- `lecons` - Leçons
- `quiz` - Quiz
- `questions` - Questions
- `exercices` - Exercices
- `enrollments` - Inscriptions
- `user_xp` - XP et niveaux
- `badges` - Badges
- `user_badges` - Badges obtenus
- `challenges` - Défis
- `user_challenges` - Progression défis
- `parcours_apprentissage` - Parcours
- `parcours_etapes` - Étapes parcours
- `parcours_inscriptions` - Inscriptions parcours

### Migrations SQL

**Scripts de Migration** (`migration_*.sql`) :

- `migration_add_admin_user.sql` - Ajout du rôle admin
- `migration_add_gamification_tables.sql` - Tables gamification
- `migration_add_parcours_apprentissage.sql` - Tables parcours
- `migration_add_level_notifications.sql` - Notifications de niveau
- `migration_add_badge_challenge_notifications.sql` - Notifications badges/défis

**Scripts de Fix** (`fix_*.sql`) :

- Corrections de données
- Réparation de contraintes
- Nettoyage de doublons

**Scripts de Test** (`test_*.sql`) :

- Tests d'intégration SQL
- Validation de la logique métier
- Génération de données de test

**Scripts de Debug** (`debug_*.sql`) :

- Diagnostic de problèmes
- Analyse de données
- Vérification de cohérence

---

## 🔧 Configuration

### application.properties

**Base de Données** :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/9awi_niveau
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

**JWT** :

```properties
jwt.secret=mySuperSecretKey...
jwt.expiration=86400000  # 24 heures
```

**OAuth2 Google** :

```properties
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
```

**Upload de Fichiers** :

```properties
upload.dir=uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

**Cache** :

```properties
spring.cache.type=simple
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10s
```

---

## 🐳 Conteneurisation Docker

### Dockerfile

**Build Multi-Stage** :

1. **Stage Build** : Compilation Maven
2. **Stage Run** : Exécution avec JRE

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

**Services** :

1. **mysql** : Base de données MySQL 8.0
2. **backend** : Application Spring Boot
3. **frontend** : Application Angular

**Réseau** :

- Bridge network `app-network`
- Communication inter-conteneurs

**Volumes** :

- `mysql_data` : Persistance des données
- `./backend/uploads` : Fichiers uploadés

---

## 📊 Patterns & Bonnes Pratiques

### Design Patterns Utilisés

**1. Repository Pattern**

- Abstraction de l'accès aux données
- Séparation des préoccupations

**2. Service Layer Pattern**

- Logique métier centralisée
- Réutilisabilité du code

**3. DTO Pattern**

- Séparation entités/DTOs
- Contrôle des données exposées

**4. Event-Driven Architecture**

- Événements applicatifs (`CourseCompletedEvent`, `QuizCompletedEvent`)
- Listeners asynchrones
- Découplage des composants

**5. Aspect-Oriented Programming (AOP)**

- `RecommendationUpdateAspect` - Interception des événements
- Séparation des préoccupations transversales

**6. Dependency Injection**

- Inversion de contrôle (IoC)
- Testabilité améliorée

### Bonnes Pratiques

**Sécurité** :

- Validation des entrées (`@Valid`, `@NotNull`, `@Email`)
- Hachage des mots de passe (BCrypt)
- Protection CSRF désactivée (API stateless)
- CORS configuré pour le frontend

**Performance** :

- Cache Spring (`@Cacheable`)
- Requêtes optimisées (fetch joins)
- Pagination des résultats
- Lazy loading JPA

**Maintenabilité** :

- Code DRY (Don't Repeat Yourself)
- Nommage explicite
- Commentaires JavaDoc
- Séparation des responsabilités

**Testabilité** :

- Services testables unitairement
- Repositories testables avec H2
- Mocks pour les dépendances externes

---

## 🚀 Déploiement

### Environnement de Développement

```bash
# Démarrer MySQL
docker-compose up mysql -d

# Lancer l'application
mvn spring-boot:run

# Ou avec Docker
docker-compose up backend
```

### Environnement de Production

```bash
# Build de l'image
docker build -t 9awi-backend:latest .

# Déploiement avec docker-compose
docker-compose up -d

# Vérification des logs
docker logs 9awi-backend
```

### Variables d'Environnement

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/9awi_niveau
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
JWT_SECRET=...
BREVO_API_KEY=...
AI_API_KEY=...
```

---

## 📈 Monitoring & Logs

### Logs

**Configuration** :

```properties
spring.jpa.show-sql=false  # Désactivé en production
logging.level.com.kawi_niveau=INFO
```

**Logs Applicatifs** :

- Authentification (login, logout)
- Erreurs de validation
- Exceptions métier
- Appels API externes

### Métriques

**Spring Boot Actuator** (à activer) :

- `/actuator/health` - Santé de l'application
- `/actuator/metrics` - Métriques JVM
- `/actuator/info` - Informations application

---

## 🧪 Tests

### Structure des Tests

```
src/test/java/com/kawi_niveau/backend/
├── controller/    # Tests d'intégration API
├── service/       # Tests unitaires services
└── repository/    # Tests repositories
```

### Types de Tests

**Tests Unitaires** :

- Services avec mocks
- Logique métier isolée

**Tests d'Intégration** :

- Controllers avec MockMvc
- Repositories avec H2

**Tests SQL** :

- Scripts `test_*.sql`
- Validation de la logique SQL

---

## 📚 Documentation API

### Endpoints Principaux

**Authentification** :

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/oauth2/google
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

**Cours** :

```
GET /api/cours
GET /api/cours/{id}
POST /api/cours (FORMATEUR)
PUT /api/cours/{id} (FORMATEUR)
DELETE /api/cours/{id} (FORMATEUR)
```

**Gamification** :

```
GET /api/gamification/stats
GET /api/gamification/leaderboard
GET /api/gamification/badges
GET /api/gamification/challenges
```

**Parcours** :

```
GET /api/parcours
GET /api/parcours/{id}
POST /api/parcours/{id}/enroll
GET /api/parcours/{id}/progression
```

**Recommandations** :

```
GET /api/recommendations/me
GET /api/recommendations/user/{userId}
POST /api/recommendations/refresh
```

**Certificats** :

```
GET /api/certificates/cours/{coursId}
GET /api/certificates/download/{certificateId}
```

**Chatbot** :

```
POST /api/chatbot/ask
```

---

## 🔮 Évolutions Futures

### Fonctionnalités Prévues

1. **Notifications Push** - Firebase Cloud Messaging
2. **Messagerie Interne** - Chat entre apprenants/formateurs
3. **Visioconférence** - Intégration Zoom/Jitsi
4. **Paiements** - Stripe/PayPal pour cours premium
5. **Analytics Avancés** - Tableau de bord formateur
6. **Mobile App** - API REST déjà prête
7. **Multilingue** - i18n backend
8. **SSO** - Single Sign-On entreprise

### Optimisations Techniques

1. **Redis Cache** - Cache distribué
2. **Elasticsearch** - Recherche full-text
3. **RabbitMQ** - File de messages asynchrone
4. **Kubernetes** - Orchestration conteneurs
5. **Monitoring** - Prometheus + Grafana
6. **CI/CD** - GitHub Actions / GitLab CI

---

## 📞 Support & Maintenance

### Logs de Débogage

**Activer les logs SQL** :

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Logs détaillés** :

```properties
logging.level.com.kawi_niveau=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Scripts Utiles

**Vérification de la structure** :

```sql
-- Voir check_table_structure.sql
```

**Diagnostic gamification** :

```sql
-- Voir debug_badges_status.sql
-- Voir debug_leaderboard.sql
```

**Nettoyage** :

```sql
-- Voir clean_old_certificate_notifications.sql
-- Voir fix_user_xp_duplicates.sql
```

---

## 🎓 Conclusion

Le backend de **9awi Niveau** est une application robuste et scalable, construite avec les meilleures pratiques Spring Boot. L'architecture en couches assure une séparation claire des responsabilités, facilitant la maintenance et l'évolution du projet.

**Points Forts** :

- ✅ Architecture modulaire et extensible
- ✅ Sécurité renforcée (JWT + OAuth2)
- ✅ Gamification complète et engageante
- ✅ Recommandations IA personnalisées
- ✅ Parcours d'apprentissage adaptatifs
- ✅ Conteneurisation Docker
- ✅ API REST bien documentée

**Technologies Modernes** :

- Spring Boot 3.5.7
- Java 17
- MySQL 8.0
- JWT + OAuth2
- Python ML
- Docker

---

**📅 Dernière mise à jour** : Janvier 2025  
**👨‍💻 Équipe** : 9awi Niveau Development Team
