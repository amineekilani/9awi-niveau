# 🎓 9awi Niveau - Plateforme d'Apprentissage Gamifiée

<div align="center">

![Logo 9awi Niveau](frontend/public/Logo_9awi_Niveau.png)

**Une plateforme d'apprentissage en ligne moderne avec gamification complète, parcours personnalisés et recommandations intelligentes**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20.2.0-red.svg)](https://angular.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📋 Table des matières

- [À propos du projet](#-à-propos-du-projet)
- [Fonctionnalités principales](#-fonctionnalités-principales)
- [Technologies utilisées](#-technologies-utilisées)
- [Architecture](#-architecture)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Utilisation](#-utilisation)
- [Documentation](#-documentation)
- [Équipe](#-équipe)
- [Licence](#-licence)

---

## 🎯 À propos du projet

**9awi Niveau** est une plateforme d'apprentissage en ligne (LMS - Learning Management System) complète développée dans le cadre du **Projet d'Intégration** encadré par **Madame Lamia Mansouri**.

La plateforme offre une expérience d'apprentissage moderne et engageante grâce à :

- 🎮 **Gamification complète** : Système de points XP, niveaux, badges et défis
- 🛤️ **Parcours d'apprentissage structurés** : Progression guidée avec validation automatique
- 🤖 **Recommandations intelligentes** : Suggestions personnalisées basées sur l'IA
- 📜 **Certification automatique** : Génération de certificats PDF professionnels
- 👥 **Multi-rôles** : Support pour Apprenants, Formateurs et Administrateurs
- 💬 **Assistant IA** : Chatbot intelligent pour l'aide contextuelle

### 🎓 Contexte académique

- **Matière** : Projet d'Intégration
- **Encadrante** : Madame Lamia Mansouri
- **Institution** : [Votre institution]
- **Année académique** : 2024-2025

---

## ✨ Fonctionnalités principales

### 👤 Pour les Apprenants

- ✅ **Inscription et authentification sécurisée** (locale + Google OAuth 2.0)
- ✅ **Catalogue de cours** avec recherche et filtrage avancés
- ✅ **Suivi de progression** en temps réel avec statistiques détaillées
- ✅ **Quiz et exercices interactifs** avec correction automatique
- ✅ **Parcours d'apprentissage personnalisés** avec déblocage progressif
- ✅ **Système de gamification** : XP, niveaux (1-10), badges, défis
- ✅ **Classement et leaderboard** pour stimuler la compétition
- ✅ **Recommandations intelligentes** de cours et parcours
- ✅ **Certificats PDF** générés automatiquement
- ✅ **Chatbot IA** pour assistance contextuelle

### 👨‍🏫 Pour les Formateurs

- ✅ **Création de cours** avec éditeur riche et upload d'images
- ✅ **Structuration du contenu** : Cours → Modules → Leçons
- ✅ **Création de quiz et exercices** avec correction automatique
- ✅ **Conception de parcours d'apprentissage** avec conditions de validation
- ✅ **Suivi détaillé des apprenants** avec analytics de performance
- ✅ **Dashboard formateur** avec statistiques en temps réel
- ✅ **Gestion des certificats** pour les parcours

### 👨‍💼 Pour les Administrateurs

- ✅ **Dashboard administrateur** avec métriques en temps réel
- ✅ **Gestion complète des utilisateurs** (CRUD, activation, rôles)
- ✅ **Configuration de la gamification** : badges, défis, niveaux
- ✅ **Gestion des classements** et analytics d'engagement
- ✅ **Export de données** (CSV) pour reporting
- ✅ **Monitoring de la plateforme** avec graphiques interactifs

---

## 🛠 Technologies utilisées

### Backend

| Technologie                | Version | Utilisation                              |
| -------------------------- | ------- | ---------------------------------------- |
| **Spring Boot**            | 3.5.7   | Framework principal                      |
| **Java**                   | 17      | Langage de programmation                 |
| **Spring Security**        | -       | Authentification et autorisation         |
| **JWT**                    | 0.11.5  | Gestion des tokens d'authentification    |
| **Spring Data JPA**        | -       | ORM et accès aux données                 |
| **MySQL**                  | 8.0     | Base de données relationnelle            |
| **OAuth2 Client**          | -       | Authentification Google                  |
| **Brevo (Sendinblue) API** | 6.0.0   | Envoi d'emails transactionnels           |
| **Apache PDFBox**          | 2.0.29  | Génération de certificats PDF            |
| **Python**                 | 3.8+    | Moteur de recommandations IA             |
| **Scikit-learn**           | -       | Machine Learning (filtrage collaboratif) |
| **Maven**                  | 3.9     | Gestionnaire de dépendances              |
| **Lombok**                 | -       | Réduction du code boilerplate            |

### Frontend

| Technologie      | Version  | Utilisation                  |
| ---------------- | -------- | ---------------------------- |
| **Angular**      | 20.2.0   | Framework frontend           |
| **TypeScript**   | 5.9.2    | Langage de programmation     |
| **Tailwind CSS** | 7.0.3    | Framework CSS utility-first  |
| **RxJS**         | 7.8.0    | Programmation réactive       |
| **Chart.js**     | 4.5.1    | Graphiques et visualisations |
| **SweetAlert2**  | 11.26.17 | Modales et alertes élégantes |
| **Angular CLI**  | 20.2.2   | Outil de ligne de commande   |

### DevOps & Déploiement

| Technologie        | Version | Utilisation                    |
| ------------------ | ------- | ------------------------------ |
| **Docker**         | -       | Conteneurisation               |
| **Docker Compose** | -       | Orchestration multi-conteneurs |
| **Nginx**          | -       | Serveur web pour le frontend   |

---

## 🏗 Architecture

### Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND (Angular)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Apprenant│  │Formateur │  │  Admin   │  │ Chatbot  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │   API REST    │
                    │   (JWT Auth)  │
                    └───────┬───────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Controllers│  │ Services │  │Repository│  │ Entities │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Modules Métier                                       │  │
│  │  • Authentification & Sécurité (JWT, OAuth2)         │  │
│  │  • Gestion des cours (CRUD, progression)             │  │
│  │  • Gamification (XP, badges, défis, leaderboard)     │  │
│  │  • Parcours d'apprentissage (étapes, validation)     │  │
│  │  • Recommandations IA (Python ML)                    │  │
│  │  • Certificats (génération PDF)                      │  │
│  │  • Chatbot (OpenRouter AI)                           │  │
│  │  • Emails (Brevo/Sendinblue)                         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │  MySQL 8.0    │
                    │  (30+ tables) │
                    └───────────────┘
```

### Architecture Backend (Couches)

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                       │
│  Controllers (43) : API REST endpoints                       │
│  • AuthController, CoursController, ParcoursController...    │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE SERVICE                          │
│  Services (50+) : Logique métier                             │
│  • GamificationService, RecommendationService...             │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE REPOSITORY                         │
│  Repositories (28) : Accès aux données (Spring Data JPA)    │
│  • UserRepository, CoursRepository, BadgeRepository...       │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE ENTITÉS                          │
│  Entities (38) : Modèle de données                           │
│  • User, Cours, Badge, Parcours, UserXP...                   │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Frontend (Standalone Components)

```
┌─────────────────────────────────────────────────────────────┐
│                    APP COMPONENT (Root)                      │
│                    • Router Outlet                           │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼───────┐  ┌────────▼────────┐  ┌──────▼──────┐
│   APPRENANT   │  │   FORMATEUR     │  │    ADMIN    │
│  (20+ comps)  │  │   (10+ comps)   │  │  (10+ comps)│
└───────────────┘  └─────────────────┘  └─────────────┘
        │                   │                   │
┌───────▼───────────────────▼───────────────────▼───────┐
│              SERVICES (30+)                            │
│  • AuthService, CoursService, GamificationService...   │
└────────────────────────────────────────────────────────┘
        │
┌───────▼───────┐
│ HTTP CLIENT   │
│ (JWT Intercep)│
└───────────────┘
```

Pour plus de détails, consultez :

- [Architecture Backend détaillée](ARCHITECTURE_BACKEND.md)
- [Architecture Frontend détaillée](ARCHITECTURE_FRONTEND.md)
- [Rapport complet des fonctionnalités](RAPPORT_FONCTIONNALITES_COMPLETE.md)

---

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Pour le développement local

- **Java 17** ou supérieur ([Télécharger](https://www.oracle.com/java/technologies/downloads/))
- **Node.js 18+** et **npm** ([Télécharger](https://nodejs.org/))
- **MySQL 8.0** ([Télécharger](https://dev.mysql.com/downloads/))
- **Maven 3.9+** ([Télécharger](https://maven.apache.org/download.cgi))
- **Python 3.8+** (pour le moteur de recommandations) ([Télécharger](https://www.python.org/downloads/))
- **Angular CLI** : `npm install -g @angular/cli`

### Pour le déploiement avec Docker

- **Docker** ([Télécharger](https://www.docker.com/get-started))
- **Docker Compose** (inclus avec Docker Desktop)

---

## 🚀 Installation

### Option 1 : Installation avec Docker (Recommandé)

La méthode la plus simple pour démarrer l'application complète.

```bash
# 1. Cloner le repository
git clone https://github.com/votre-username/9awi-niveau.git
cd 9awi-niveau

# 2. Lancer tous les services avec Docker Compose
docker-compose up -d

# 3. Vérifier que les conteneurs sont démarrés
docker-compose ps

# L'application sera accessible sur :
# - Frontend : http://localhost:4200
# - Backend API : http://localhost:8080
# - MySQL : localhost:3306
```

**Services démarrés :**

- `9awi-mysql` : Base de données MySQL 8.0
- `9awi-backend` : API Spring Boot (port 8080)
- `9awi-frontend` : Application Angular (port 4200)

### Option 2 : Installation manuelle (Développement)

Pour un environnement de développement avec hot-reload.

#### Backend

```bash
# 1. Naviguer vers le dossier backend
cd backend

# 2. Installer les dépendances Python pour les recommandations
cd src/main/python
pip install -r requirements.txt
cd ../../..

# 3. Configurer la base de données MySQL
# Créer une base de données nommée '9awi_niveau'
mysql -u root -p
CREATE DATABASE 9awi_niveau;
EXIT;

# 4. Configurer application.properties
# Éditer backend/src/main/resources/application.properties
# Ajuster les paramètres de connexion MySQL si nécessaire

# 5. Compiler et lancer le backend
mvn clean install
mvn spring-boot:run

# Le backend sera accessible sur http://localhost:8080
```

#### Frontend

```bash
# 1. Naviguer vers le dossier frontend
cd frontend

# 2. Installer les dépendances npm
npm install

# 3. Lancer le serveur de développement
npm start

# Le frontend sera accessible sur http://localhost:4200
```

---

## ⚙️ Configuration

### Configuration Backend

Fichier : `backend/src/main/resources/application.properties`

```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/9awi_niveau
spring.datasource.username=root
spring.datasource.password=root

# JWT
jwt.secret=votre_secret_jwt_tres_long_et_securise
jwt.expiration=86400000  # 24 heures

# OAuth2 Google
spring.security.oauth2.client.registration.google.client-id=votre_client_id
spring.security.oauth2.client.registration.google.client-secret=votre_client_secret

# Brevo (Emails)
brevo.api.key=votre_cle_api_brevo
brevo.sender.email=votre_email@example.com
brevo.sender.name=9awi Niveau

# OpenRouter AI (Chatbot)
ai.api.key=votre_cle_api_openrouter
ai.api.url=https://openrouter.ai/api/v1/chat/completions
ai.model=meta-llama/llama-3.2-3b-instruct:free

# Upload de fichiers
upload.dir=uploads
spring.servlet.multipart.max-file-size=10MB
```

### Configuration Frontend

Fichier : `frontend/src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
};
```

Pour la production : `frontend/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: "/api", // Utilise le proxy Nginx
};
```

### Variables d'environnement Docker

Fichier : `docker-compose.yml` (déjà configuré)

Les variables d'environnement sont définies dans le fichier docker-compose.yml. Vous pouvez les modifier selon vos besoins.

---

## 💻 Utilisation

### Accès à l'application

Une fois l'application démarrée, accédez à :

**Frontend** : [http://localhost:4200](http://localhost:4200)

### Comptes de test

#### Administrateur

```
Email : admin@9awiniveau.com
Mot de passe : admin123
```

#### Formateur

```
Email : formateur@9awiniveau.com
Mot de passe : formateur123
```

#### Apprenant

```
Email : apprenant@9awiniveau.com
Mot de passe : apprenant123
```

### Fonctionnalités par rôle

#### 👤 Apprenant

1. **S'inscrire** ou **se connecter** (email/mot de passe ou Google)
2. **Explorer le catalogue** de cours et parcours
3. **S'inscrire** aux cours et parcours d'intérêt
4. **Suivre les leçons** et compléter les modules
5. **Passer les quiz** et exercices interactifs
6. **Gagner des XP**, monter de niveau et débloquer des badges
7. **Consulter les recommandations** personnalisées
8. **Télécharger les certificats** après complétion des parcours
9. **Utiliser le chatbot** pour obtenir de l'aide

#### 👨‍🏫 Formateur

1. **Créer des cours** avec modules et leçons
2. **Ajouter des quiz** et exercices interactifs
3. **Concevoir des parcours** d'apprentissage structurés
4. **Suivre la progression** des apprenants
5. **Consulter les statistiques** de performance
6. **Gérer les certificats** pour les parcours

#### 👨‍💼 Administrateur

1. **Gérer les utilisateurs** (création, modification, suppression)
2. **Configurer la gamification** (badges, défis, niveaux)
3. **Consulter les analytics** de la plateforme
4. **Gérer les classements** et leaderboards
5. **Exporter les données** pour reporting
6. **Monitorer l'engagement** des utilisateurs

---

## 📚 Documentation

### Documentation technique

- [Architecture Backend](ARCHITECTURE_BACKEND.md) - Architecture détaillée du backend Spring Boot
- [Architecture Frontend](ARCHITECTURE_FRONTEND.md) - Architecture détaillée du frontend Angular
- [Rapport des fonctionnalités](RAPPORT_FONCTIONNALITES_COMPLETE.md) - Liste complète des fonctionnalités implémentées

### API Documentation

L'API REST est documentée et accessible via les endpoints suivants :

**Base URL** : `http://localhost:8080/api`

**Endpoints principaux** :

- `/api/auth/*` - Authentification (login, register, OAuth2)
- `/api/cours/*` - Gestion des cours
- `/api/modules/*` - Gestion des modules
- `/api/lecons/*` - Gestion des leçons
- `/api/quiz/*` - Gestion des quiz
- `/api/exercices/*` - Gestion des exercices
- `/api/parcours/*` - Gestion des parcours d'apprentissage
- `/api/gamification/*` - Système de gamification
- `/api/recommendations/*` - Recommandations personnalisées
- `/api/certificates/*` - Génération de certificats
- `/api/chatbot/*` - Assistant IA
- `/api/admin/*` - Administration

### Scripts SQL

Le dossier `backend/` contient de nombreux scripts SQL pour :

- **Migrations** : `migration_*.sql` - Scripts de migration de la base de données
- **Tests** : `test_*.sql` - Scripts de test et validation
- **Debug** : `debug_*.sql` - Scripts de diagnostic
- **Fix** : `fix_*.sql` - Scripts de correction de données

---

## 👥 Équipe

Ce projet a été développé par :

| Nom                   | Rôle                   | GitHub / Contact                    |
| --------------------- | ---------------------- | ----------------------------------- |
| **Baha Eddine Manai** | Développeur Full Stack | [@BahaManai](https://github.com)    |
| **Amine Kilani**      | Développeur Full Stack | [@amineekilani](https://github.com) |

**Encadrante** : Madame Lamia Mansouri

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 🙏 Remerciements

Nous tenons à remercier :

- **Madame Lamia Mansouri** pour son encadrement et ses conseils précieux
- **Notre institution** pour le cadre et les ressources fournis
- **La communauté open source** pour les technologies et bibliothèques utilisées

---

## 📞 Support et Contact

Pour toute question ou problème :

- 📧 Email : bahaeddinmanai7@gmail.com
- 🐛 Issues : [GitHub Issues](https://github.com/amineekilani/9awi-niveau/issues)

---

## 🔮 Roadmap et Évolutions futures

### Fonctionnalités prévues

- 📱 **Application mobile** (React Native / Flutter)
- 🔔 **Notifications push** (Firebase Cloud Messaging)
- 💬 **Messagerie interne** entre apprenants et formateurs
- 🎥 **Visioconférence** intégrée (Zoom/Jitsi)
- 💳 **Système de paiement** pour cours premium (Stripe/PayPal)
- 🌍 **Multilingue** (i18n)
- 📊 **Analytics avancés** pour formateurs
- 🔍 **Recherche full-text** (Elasticsearch)
- 🚀 **Optimisations** (Redis cache, RabbitMQ)

---

<div align="center">

**Développé avec ❤️ par l'équipe 9awi Niveau**

⭐ Si vous aimez ce projet, n'hésitez pas à lui donner une étoile sur GitHub !

</div>
