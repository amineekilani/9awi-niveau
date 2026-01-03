# 🤖 Guide d'Accès aux Recommandations IA

## 📍 Comment Consulter les Recommandations

### 1. **Accès Direct par URL**

```
http://localhost:4200/recommandations
```

Tapez cette URL directement dans votre navigateur une fois connecté.

### 2. **Via le Menu Principal** ⭐ **NOUVEAU**

Dans la barre de navigation en haut de la page d'accueil, cliquez sur :

```
🤖 Recommandations
```

### 3. **Via le Dashboard Principal** ⭐ **NOUVEAU**

Sur la page d'accueil, cliquez sur la nouvelle carte :

```
┌─────────────────────────┐
│ 🤖 IA Recommandations   │
│ Nouveau                 │
│ Contenu personnalisé    │
│ ✨ 🎯 📚               │
│ Découvrez maintenant !  │
└─────────────────────────┘
```

## 🎯 Fonctionnalités Disponibles

### Interface de Recommandations

- ✅ **Recommandations personnalisées** basées sur votre profil
- ✅ **Filtres intelligents** (type, confiance, priorité)
- ✅ **Explications pédagogiques** pour chaque suggestion
- ✅ **Navigation directe** vers les contenus recommandés
- ✅ **Actualisation** en temps réel
- ✅ **Interface responsive** (mobile/desktop)

### Types de Recommandations

- 📚 **COURS** - Cours complets adaptés à votre niveau
- ▶️ **LECON** - Leçons spécifiques pour renforcer vos acquis
- ❓ **QUIZ** - Évaluations pour tester vos connaissances
- 🏆 **CHALLENGE** - Défis gamifiés pour rester motivé

## 🔧 Intégration Complète

### Navigation Mise à Jour

Le lien "🤖 Recommandations" a été ajouté dans :

- ✅ **Menu principal** (barre de navigation)
- ✅ **Dashboard** (nouvelle carte dédiée)
- ✅ **Routes Angular** (`/recommandations`)

### Design Cohérent

- 🎨 **Style uniforme** avec le reste de l'application
- 🌈 **Couleurs harmonieuses** (gradient cyan/teal)
- ✨ **Animations** et effets visuels
- 📱 **Responsive** sur tous les appareils

## 🚀 Démarrage Rapide

### Étape 1 : Démarrer l'Application

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
ng serve
```

### Étape 2 : Se Connecter

1. Allez sur `http://localhost:4200`
2. Connectez-vous avec vos identifiants

### Étape 3 : Accéder aux Recommandations

**Option A :** Cliquez sur "🤖 Recommandations" dans le menu
**Option B :** Cliquez sur la carte "IA Recommandations" du dashboard
**Option C :** Allez directement sur `/recommandations`

## 🎓 Utilisation de l'Agent IA

### Première Utilisation

1. **Chargement automatique** de vos recommandations personnalisées
2. **Message d'encouragement** adapté à votre profil
3. **Suggestions intelligentes** basées sur votre progression

### Filtres Disponibles

- **Type de contenu** : Tous, Cours, Leçons, Quiz, Challenges
- **Confiance minimale** : 0%, 50%, 70%, 90%
- **Haute priorité uniquement** : Checkbox pour les urgences

### Actions Possibles

- ▶️ **Commencer** - Navigation directe vers le contenu
- ❌ **Ignorer** - Masquer une recommandation
- 🔄 **Actualiser** - Recharger les suggestions
- ⚙️ **Personnaliser** - Paramètres avancés

## 📊 API Backend

### Endpoints Disponibles

```http
GET /api/recommendations/me              # Vos recommandations
GET /api/recommendations/user/{id}       # Pour un utilisateur (admin)
GET /api/recommendations/me/custom       # Avec paramètres
GET /api/recommendations/test            # Test du moteur (admin)
```

### Authentification

- 🔐 **JWT Token** requis
- 👤 **Rôles supportés** : ETUDIANT, FORMATEUR, ADMIN
- 🛡️ **Sécurité** : Spring Security intégrée

## 🎯 Algorithmes IA

### Méthodes de Recommandation

1. **Filtrage Collaboratif** (40%) - Utilisateurs similaires
2. **Basé sur le Contenu** (40%) - Similarité des cours
3. **Basé sur le Niveau** (20%) - Progression pédagogique

### Règles Pédagogiques

- ❌ **Jamais de contenu déjà complété**
- 📉 **Score < 50%** → Contenu de révision
- 📈 **Score > 80%** → Contenu plus avancé
- 🧩 **Respect de la hiérarchie** pédagogique
- 🎯 **Priorité à la motivation**

## 🔍 Dépannage

### Si les Recommandations ne s'Affichent Pas

1. **Vérifiez la connexion** - Êtes-vous connecté ?
2. **Rechargez la page** - F5 ou Ctrl+R
3. **Vérifiez la console** - F12 → Console pour les erreurs
4. **Backend actif** - Le serveur Spring Boot fonctionne-t-il ?

### Messages d'Erreur Courants

- **"Impossible de charger"** → Problème de connexion API
- **"Aucune recommandation"** → Profil utilisateur incomplet
- **"Erreur 401"** → Token JWT expiré, reconnectez-vous

## 🎉 Félicitations !

Vous avez maintenant accès à un **système de recommandation IA complet** qui :

- 🧠 **Apprend** de votre comportement d'apprentissage
- 🎯 **Personnalise** les suggestions selon votre profil
- 📈 **Optimise** votre progression pédagogique
- 🚀 **Améliore** votre expérience d'apprentissage

**L'Agent IA de Recommandation Pédagogique est maintenant opérationnel !** 🎓
