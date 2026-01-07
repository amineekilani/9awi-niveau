# 🎨 Architecture Frontend - 9awi Niveau

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Technologies utilisées](#technologies-utilisées)
3. [Structure du projet](#structure-du-projet)
4. [Configuration](#configuration)
5. [Composants principaux](#composants-principaux)
6. [Services](#services)
7. [Guards et Intercepteurs](#guards-et-intercepteurs)
8. [Routing](#routing)
9. [Assets et ressources](#assets-et-ressources)

---

## 🎯 Vue d'ensemble

Le frontend de **9awi Niveau** est une application Angular 20.2.0 moderne construite avec TypeScript 5.9.2, utilisant une architecture **standalone components** (sans NgModules). L'application suit les dernières pratiques Angular avec des composants autonomes, des signals pour la réactivité, et une architecture modulaire par fonctionnalités.

### Caractéristiques principales

- **Architecture standalone** : Pas de modules NgModule, composants autonomes
- **TypeScript strict** : Configuration stricte pour la sécurité du code
- **Reactive Programming** : Utilisation de RxJS pour la gestion asynchrone
- **Responsive Design** : Interface adaptative avec Tailwind CSS
- **Gamification complète** : Badges, niveaux, défis, classements
- **Multi-rôles** : Support pour Apprenants, Formateurs et Administrateurs
- **Parcours d'apprentissage** : Système de parcours structurés avec progression
- **Recommandations intelligentes** : Suggestions de cours personnalisées

---

## 🛠 Technologies utilisées

### Framework et Core

- **Angular 20.2.0** - Framework principal
- **TypeScript 5.9.2** - Langage de programmation
- **RxJS 7.8.0** - Programmation réactive
- **Zone.js 0.15.0** - Détection de changements

### UI et Styling

- **Tailwind CSS** (@ngneat/tailwind 7.0.3) - Framework CSS utility-first
- **Chart.js 4.5.1** - Bibliothèque de graphiques et visualisations
- **SweetAlert2 11.26.17** - Modales et alertes élégantes

### Outils de développement

- **Angular CLI 20.2.2** - Outil de ligne de commande
- **Vite** - Build tool ultra-rapide (via @angular/build)
- **Karma & Jasmine** - Framework de tests unitaires
- **TypeScript Compiler** - Compilation et vérification de types

### Déploiement

- **Nginx** - Serveur web de production
- **Docker** - Conteneurisation de l'application

---

## 📁 Structure du projet

```
frontend/
├── src/
│   ├── app/                          # Code source de l'application
│   │   ├── admin-dashboard/          # Dashboard admin simple
│   │   ├── admin-dashboard-enhanced/ # Dashboard admin amélioré avec graphiques
│   │   ├── admin-dashboard-overview/ # Vue d'ensemble statistiques admin
│   │   ├── admin-gamification/       # Gestion gamification (badges, niveaux, défis)
│   │   ├── admin-layout/             # Layout commun pour l'interface admin
│   │   ├── admin-main/               # Composant principal admin avec navigation
│   │   ├── admin-users/              # Gestion des utilisateurs
│   │   ├── badge-management/         # Gestion des badges
│   │   ├── challenge-management/     # Gestion des défis
│   │   ├── chatbot/                  # Assistant conversationnel IA
│   │   ├── classement/               # Leaderboard / Classement
│   │   ├── components/               # Composants réutilisables (chart, etc.)
│   │   ├── confirm-delete/           # Modal de confirmation de suppression
│   │   ├── constants/                # Constantes (domaines, etc.)
│   │   ├── cours-detail/             # Page de détail d'un cours
│   │   ├── cours-form/               # Formulaire de création/édition de cours
│   │   ├── cours-list/               # Liste des cours disponibles
│   │   ├── exercice-creator/         # Créateur d'exercices interactifs
│   │   ├── exercice-viewer/          # Visualiseur d'exercices
│   │   ├── forgot-password/          # Récupération de mot de passe
│   │   ├── formateur-dashboard/      # Dashboard formateur
│   │   ├── home/                     # Page d'accueil utilisateur
│   │   ├── leaderboard-management/   # Gestion du leaderboard (admin)
│   │   ├── level-management/         # Gestion des niveaux (admin)
│   │   ├── login/                    # Page de connexion
│   │   ├── mes-cours/                # Mes cours (apprenant)
│   │   ├── mes-defis/                # Mes défis en cours
│   │   ├── mes-parcours/             # Mes parcours d'apprentissage
│   │   ├── mes-recompenses/          # Mes récompenses et badges
│   │   ├── mes-reussites/            # Mes réussites et certificats
│   │   ├── module-detail/            # Détail d'un module de cours
│   │   ├── navbar/                   # Barre de navigation
│   │   ├── niveau-badge/             # Composant niveau et badge
│   │   ├── parcours-catalogue/       # Catalogue des parcours disponibles
│   │   ├── parcours-dashboard/       # Dashboard de gestion des parcours
│   │   ├── parcours-detail/          # Détail d'un parcours
│   │   ├── parcours-etapes/          # Gestion des étapes d'un parcours
│   │   ├── parcours-form/            # Formulaire de création/édition de parcours
│   │   ├── parcours-manager/         # Gestionnaire de parcours (formateur)
│   │   ├── parcours-progression-details/ # Détails de progression dans un parcours
│   │   ├── profile/                  # Profil utilisateur
│   │   ├── quiz-viewer/              # Visualiseur de quiz
│   │   ├── recommendations/          # Recommandations de cours
│   │   ├── register/                 # Page d'inscription
│   │   ├── reset-password/           # Réinitialisation de mot de passe
│   │   ├── services/                 # Services métier additionnels
│   │   ├── simple-dashboard/         # Dashboard simple
│   │   ├── test-charts/              # Tests de graphiques
│   │   ├── test-niveaux/             # Tests de niveaux
│   │   ├── user-modal/               # Modal de gestion utilisateur
│   │   ├── verify-email/             # Vérification d'email
│   │   │
│   │   ├── app.ts                    # Composant racine de l'application
│   │   ├── app.routes.ts             # Configuration du routing
│   │   ├── app.config.ts             # Configuration de l'application
│   │   ├── app.html                  # Template du composant racine
│   │   ├── app.css                   # Styles du composant racine
│   │   │
│   │   ├── *.service.ts              # Services (30+ services)
│   │   ├── *-guard.ts                # Guards de navigation
│   │   └── jwt.interceptor.ts        # Intercepteur JWT
│   │
│   ├── assets/                       # Ressources statiques
│   │   └── login.jpg                 # Image de fond login
│   ├── environments/                 # Configurations d'environnement
│   │   ├── environment.ts            # Environnement de développement
│   │   └── environment.prod.ts       # Environnement de production
│   ├── index.html                    # Page HTML principale
│   ├── main.ts                       # Point d'entrée de l'application
│   └── styles.css                    # Styles globaux (Tailwind)
│
├── public/                           # Fichiers publics statiques
│   ├── badges/                       # Images des badges
│   │   ├── default-badge.svg
│   │   ├── first-course.svg
│   │   ├── perfect-score.svg
│   │   ├── quiz-master.svg
│   │   └── streak-master.svg
│   ├── favicon.ico                   # Icône du site
│   ├── login.jpg                     # Image de login
│   └── Logo_9awi_Niveau.png          # Logo de l'application
│
├── angular.json                      # Configuration Angular CLI
├── package.json                      # Dépendances npm
├── tsconfig.json                     # Configuration TypeScript
├── tsconfig.app.json                 # Config TypeScript pour l'app
├── tsconfig.spec.json                # Config TypeScript pour les tests
├── proxy.conf.json                   # Configuration du proxy de développement
├── Dockerfile                        # Configuration Docker
├── nginx.conf                        # Configuration Nginx
└── README.md                         # Documentation
```

---

## ⚙️ Configuration

### 1. Configuration Angular (angular.json)

Le fichier `angular.json` définit la configuration du projet Angular CLI.

**Points clés :**

- **Builder** : `@angular/build:application` (nouveau builder basé sur Vite)
- **Output** : `dist/frontend`
- **Polyfills** : Zone.js uniquement
- **Assets** : Dossier `public/` copié automatiquement
- **Styles** : `src/styles.css` (avec Tailwind CSS)
- **Proxy** : Configuration via `proxy.conf.json` pour le développement

**Configurations de build :**

- **Production** : Optimisation, hashing des fichiers, budgets de taille (2MB initial, 10kB par style)
- **Development** : Source maps activés, pas d'optimisation, extraction de licences désactivée

### 2. Configuration TypeScript (tsconfig.json)

**Options strictes activées :**

```typescript
{
  "strict": true,                              // Mode strict complet
  "noImplicitOverride": true,                  // Forcer l'utilisation de override
  "noPropertyAccessFromIndexSignature": true,  // Accès propriété strict
  "noImplicitReturns": true,                   // Retours explicites requis
  "noFallthroughCasesInSwitch": true          // Pas de fallthrough dans switch
}
```

**Cible de compilation :**

- **Target** : ES2022 (JavaScript moderne)
- **Module** : ES2022 (modules ES natifs)
- **Decorators** : Activés pour Angular
- **useDefineForClassFields** : false (compatibilité Angular)

### 3. Configuration du Proxy (proxy.conf.json)

Redirige les appels API vers le backend pendant le développement :

```json
{
  "/api/*": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

Tous les appels vers `/api/*` sont redirigés vers `http://localhost:8080/api/*`.

### 4. Configuration de l'application (app.config.ts)

Point central de configuration avec :

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes), // Configuration du routing
    provideHttpClient(
      withInterceptors([
        // Client HTTP
        jwtInterceptor, // Intercepteur JWT
      ])
    ),
  ],
};
```

**Providers :**

- **Router** : Gestion de la navigation
- **HttpClient** : Client HTTP avec intercepteur JWT automatique

### 5. Point d'entrée (main.ts)

Bootstrap de l'application en mode standalone :

```typescript
bootstrapApplication(App, appConfig).catch((err) => console.error(err));
```

Pas de module racine, l'application démarre directement avec le composant `App`.

---

## 🧩 Composants principaux

L'application contient plus de 40 composants organisés par fonctionnalité. Voici les catégories principales :

### 1. Authentification et Sécurité

| Composant                   | Fichier                              | Utilité                                     |
| --------------------------- | ------------------------------------ | ------------------------------------------- |
| **LoginComponent**          | `login/login.ts`                     | Page de connexion avec validation           |
| **RegisterComponent**       | `register/register.ts`               | Inscription de nouveaux utilisateurs        |
| **ForgotPasswordComponent** | `forgot-password/forgot-password.ts` | Demande de réinitialisation de mot de passe |
| **ResetPasswordComponent**  | `reset-password/reset-password.ts`   | Réinitialisation du mot de passe via token  |
| **VerifyEmailComponent**    | `verify-email/verify-email.ts`       | Vérification de l'adresse email             |

### 2. Navigation et Layout

| Composant                | Fichier                        | Utilité                              |
| ------------------------ | ------------------------------ | ------------------------------------ |
| **App**                  | `app.ts`                       | Composant racine avec RouterOutlet   |
| **NavbarComponent**      | `navbar/navbar.component.ts`   | Barre de navigation principale       |
| **HomeComponent**        | `home/home.ts`                 | Page d'accueil après connexion       |
| **AdminLayoutComponent** | `admin-layout/admin-layout.ts` | Layout commun pour l'interface admin |

### 3. Gestion des Cours

| Composant                 | Fichier                          | Utilité                               |
| ------------------------- | -------------------------------- | ------------------------------------- |
| **CoursListComponent**    | `cours-list/cours-list.ts`       | Liste de tous les cours disponibles   |
| **CoursDetailComponent**  | `cours-detail/cours-detail.ts`   | Détail d'un cours avec modules        |
| **CoursFormComponent**    | `cours-form/cours-form.ts`       | Création/édition de cours (formateur) |
| **ModuleDetailComponent** | `module-detail/module-detail.ts` | Détail d'un module avec leçons        |
| **MesCoursComponent**     | `mes-cours/mes-cours.ts`         | Cours de l'apprenant avec progression |

### 4. Exercices et Quiz

| Composant                    | Fichier                                | Utilité                           |
| ---------------------------- | -------------------------------------- | --------------------------------- |
| **QuizViewerComponent**      | `quiz-viewer/quiz-viewer.ts`           | Interface de passage de quiz      |
| **ExerciceViewerComponent**  | `exercice-viewer/exercice-viewer.ts`   | Interface d'exercices interactifs |
| **ExerciceCreatorComponent** | `exercice-creator/exercice-creator.ts` | Création d'exercices (formateur)  |

### 5. Parcours d'apprentissage

| Composant                               | Fichier                                                                  | Utilité                            |
| --------------------------------------- | ------------------------------------------------------------------------ | ---------------------------------- |
| **ParcoursCatalogueComponent**          | `parcours-catalogue/parcours-catalogue.component.ts`                     | Catalogue des parcours disponibles |
| **ParcoursDetailComponent**             | `parcours-detail/parcours-detail.component.ts`                           | Détail d'un parcours avec étapes   |
| **MesParcoursComponent**                | `mes-parcours/mes-parcours.component.ts`                                 | Parcours de l'apprenant            |
| **ParcoursFormComponent**               | `parcours-form/parcours-form.component.ts`                               | Création/édition de parcours       |
| **ParcoursManagerComponent**            | `parcours-manager/parcours-manager.component.ts`                         | Gestion des parcours (formateur)   |
| **ParcoursEtapesComponent**             | `parcours-etapes/parcours-etapes.component.ts`                           | Gestion des étapes d'un parcours   |
| **ParcoursProgressionDetailsComponent** | `parcours-progression-details/parcours-progression-details.component.ts` | Détails de progression             |
| **ParcoursDashboardComponent**          | `parcours-dashboard/parcours-dashboard.component.ts`                     | Dashboard de gestion               |

### 6. Gamification

| Composant                   | Fichier                              | Utilité                                |
| --------------------------- | ------------------------------------ | -------------------------------------- |
| **MesRecompensesComponent** | `mes-recompenses/mes-recompenses.ts` | Badges et récompenses de l'utilisateur |
| **MesDefisComponent**       | `mes-defis/mes-defis.ts`             | Défis en cours et complétés            |
| **MesReussitesComponent**   | `mes-reussites/mes-reussites.ts`     | Certificats et réussites               |
| **ClassementComponent**     | `classement/classement.ts`           | Leaderboard global                     |
| **NiveauBadgeComponent**    | `niveau-badge/niveau-badge.ts`       | Affichage niveau et badge              |

### 7. Administration

| Composant                           | Fichier                                                          | Utilité                                   |
| ----------------------------------- | ---------------------------------------------------------------- | ----------------------------------------- |
| **AdminMainComponent**              | `admin-main/admin-main.ts`                                       | Composant principal admin avec navigation |
| **AdminDashboardEnhancedComponent** | `admin-dashboard-enhanced/admin-dashboard-enhanced.component.ts` | Dashboard avec graphiques                 |
| **AdminUsersComponent**             | `admin-users/admin-users.ts`                                     | Gestion des utilisateurs                  |
| **AdminGamificationComponent**      | `admin-gamification/admin-gamification.ts`                       | Gestion de la gamification                |
| **BadgeManagementComponent**        | `badge-management/badge-management.ts`                           | Gestion des badges                        |
| **ChallengeManagementComponent**    | `challenge-management/challenge-management.ts`                   | Gestion des défis                         |
| **LevelManagementComponent**        | `level-management/level-management.ts`                           | Gestion des niveaux                       |
| **LeaderboardManagementComponent**  | `leaderboard-management/leaderboard-management.ts`               | Gestion du classement                     |

### 8. Formateur

| Composant                       | Fichier                                      | Utilité                               |
| ------------------------------- | -------------------------------------------- | ------------------------------------- |
| **FormateurDashboardComponent** | `formateur-dashboard/formateur-dashboard.ts` | Dashboard formateur avec statistiques |

### 9. Autres

| Composant                    | Fichier                                        | Utilité                                 |
| ---------------------------- | ---------------------------------------------- | --------------------------------------- |
| **ProfileComponent**         | `profile/profile.ts`                           | Profil utilisateur avec édition         |
| **RecommendationsComponent** | `recommendations/recommendations.component.ts` | Recommandations de cours personnalisées |
| **ChatbotComponent**         | `chatbot/chatbot.ts`                           | Assistant conversationnel IA            |
| **ConfirmDeleteComponent**   | `confirm-delete/confirm-delete.ts`             | Modal de confirmation de suppression    |
| **UserModalComponent**       | `user-modal/user-modal.ts`                     | Modal de gestion utilisateur            |

---

## 🔧 Services

L'application utilise plus de 30 services pour gérer la logique métier et la communication avec le backend.

### Services d'authentification et utilisateurs

| Service          | Fichier            | Utilité                                              |
| ---------------- | ------------------ | ---------------------------------------------------- |
| **AuthService**  | `auth.ts`          | Gestion de l'authentification (login, logout, token) |
| **AdminService** | `admin.service.ts` | Services d'administration                            |

### Services de contenu pédagogique

| Service             | Fichier               | Utilité                                |
| ------------------- | --------------------- | -------------------------------------- |
| **CoursService**    | `cours.service.ts`    | CRUD des cours                         |
| **ModuleService**   | `module.service.ts`   | Gestion des modules                    |
| **LeconService**    | `lecon.service.ts`    | Gestion des leçons                     |
| **QuizService**     | `quiz.service.ts`     | Gestion des quiz                       |
| **ExerciceService** | `exercice.service.ts` | Gestion des exercices                  |
| **DomaineService**  | `domaine.service.ts`  | Gestion des domaines de spécialisation |

### Services de progression et résultats

| Service                         | Fichier                            | Utilité                      |
| ------------------------------- | ---------------------------------- | ---------------------------- |
| **EnrollmentService**           | `enrollment.service.ts`            | Inscriptions aux cours       |
| **QuizResultatService**         | `quiz-resultat.service.ts`         | Résultats des quiz           |
| **ExerciceResultatService**     | `exercice-resultat.service.ts`     | Résultats des exercices      |
| **ApprenantProgressionService** | `apprenant-progression.service.ts` | Progression des apprenants   |
| **ModuleProgressService**       | `module-progress.service.ts`       | Progression dans les modules |

### Services de parcours d'apprentissage

| Service                         | Fichier                            | Utilité                           |
| ------------------------------- | ---------------------------------- | --------------------------------- |
| **ParcoursService**             | `parcours.service.ts`              | CRUD des parcours                 |
| **ParcoursProgressionService**  | `parcours-progression.service.ts`  | Progression dans les parcours     |
| **ParcoursValidationService**   | `parcours-validation.service.ts`   | Validation des étapes de parcours |
| **ParcoursAutoRefreshService**  | `parcours-auto-refresh.service.ts` | Rafraîchissement automatique      |
| **ParcoursNotificationService** | `parcours-notification.service.ts` | Notifications de parcours         |

### Services de gamification

| Service                             | Fichier                                | Utilité                            |
| ----------------------------------- | -------------------------------------- | ---------------------------------- |
| **GamificationService**             | `gamification.service.ts`              | Logique de gamification principale |
| **UserGamificationService**         | `user-gamification.service.ts`         | Gamification par utilisateur       |
| **GamificationInitService**         | `gamification-init.service.ts`         | Initialisation de la gamification  |
| **GamificationNotificationService** | `gamification-notification.service.ts` | Notifications de gamification      |
| **BadgeNotificationService**        | `badge-notification.service.ts`        | Notifications de badges            |
| **ChallengeNotificationService**    | `challenge-notification.service.ts`    | Notifications de défis             |
| **LevelNotificationService**        | `level-notification.service.ts`        | Notifications de niveau            |
| **LevelTestService**                | `level-test.service.ts`                | Tests de niveau                    |

### Services de recommandations et certificats

| Service                   | Fichier                     | Utilité                       |
| ------------------------- | --------------------------- | ----------------------------- |
| **RecommendationService** | `recommendation.service.ts` | Recommandations de cours      |
| **CertificateService**    | `certificate.service.ts`    | Génération de certificats PDF |

### Services additionnels

| Service                  | Fichier                              | Utilité                    |
| ------------------------ | ------------------------------------ | -------------------------- |
| **ChatbotService**       | `services/chatbot.service.ts`        | Service de chatbot IA      |
| **AdvancedStatsService** | `services/advanced-stats.service.ts` | Statistiques avancées      |
| **TestBackendService**   | `test-backend.service.ts`            | Tests de connexion backend |

### Architecture des services

**Caractéristiques communes :**

- **Injectable** : Tous les services sont injectables avec `@Injectable({ providedIn: 'root' })`
- **HttpClient** : Utilisation du HttpClient Angular pour les appels API
- **RxJS** : Retour d'Observables pour la gestion asynchrone
- **Error Handling** : Gestion des erreurs avec catchError
- **Type Safety** : Interfaces TypeScript pour les données

**Exemple de structure d'un service :**

```typescript
@Injectable({ providedIn: "root" })
export class CoursService {
  private apiUrl = "/api/cours";

  constructor(private http: HttpClient) {}

  getCours(): Observable<Cours[]> {
    return this.http.get<Cours[]>(this.apiUrl);
  }

  getCoursById(id: number): Observable<Cours> {
    return this.http.get<Cours>(`${this.apiUrl}/${id}`);
  }

  createCours(cours: Cours): Observable<Cours> {
    return this.http.post<Cours>(this.apiUrl, cours);
  }

  updateCours(id: number, cours: Cours): Observable<Cours> {
    return this.http.put<Cours>(`${this.apiUrl}/${id}`, cours);
  }

  deleteCours(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---
