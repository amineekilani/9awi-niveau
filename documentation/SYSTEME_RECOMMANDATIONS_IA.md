# 🤖 Système de Recommandations IA - 9awi Niveau

## 📋 Vue d'ensemble

Le système de recommandations IA de 9awi Niveau utilise l'intelligence artificielle pour analyser le profil des apprenants et leur proposer des parcours personnalisés. Il combine l'analyse des préférences utilisateur, l'historique d'apprentissage, les performances et les données de gamification pour générer des recommandations précises.

## 🎯 Objectifs

- **Personnalisation** : Proposer des parcours adaptés au profil de chaque apprenant
- **Engagement** : Augmenter la motivation par des suggestions pertinentes
- **Performance** : Améliorer les taux de completion et de réussite
- **Découverte** : Faire découvrir de nouveaux domaines d'apprentissage
- **Optimisation** : Adapter le contenu au temps disponible et aux objectifs

## 🏗️ Architecture du Système

### Backend (Spring Boot)

#### Entités Principales

**UserPreferences**

```java
- preferredCategories: JSON array des catégories préférées
- preferredDifficulty: Niveau de difficulté préféré (DEBUTANT, INTERMEDIAIRE, AVANCE, EXPERT)
- learningStyle: Style d'apprentissage (VISUAL, AUDITORY, KINESTHETIC, READING)
- timeAvailabilityHours: Heures disponibles par semaine
- learningGoals: JSON array des objectifs d'apprentissage
- interests: JSON array des centres d'intérêt
- careerFocus: Orientation professionnelle
- preferredDurationMin/Max: Fourchette de durée préférée
- challengePreference: Niveau de défi souhaité (LOW, MEDIUM, HIGH)
- certificationImportant: Importance des certificats
```

#### Services Clés

**AIRecommendationService**

- `getPersonalizedRecommendations()` : Recommandations basées sur le profil complet
- `getRecommendationsByCriteria()` : Recommandations par critères spécifiques
- `buildUserProfile()` : Construction du profil utilisateur pour l'analyse
- `calculateRecommendationScore()` : Calcul du score de recommandation

**RecommendationController**

- `GET /api/recommendations/personalized` : Recommandations personnalisées
- `POST /api/recommendations/by-criteria` : Recherche par critères
- `GET/POST /api/recommendations/preferences` : Gestion des préférences
- `GET /api/recommendations/quick` : Recommandations rapides (top 3)

### Frontend (Angular)

#### Services

**RecommendationService**

- Gestion des appels API pour les recommandations
- Utilitaires pour les préférences (parsing JSON, options disponibles)
- Cache et optimisation des requêtes

#### Composants

**RecommendationsComponent**

- Interface complète avec 3 onglets : Personnalisées, Critères, Préférences
- Affichage des scores et raisons de recommandation
- Gestion des préférences utilisateur

**Widget sur la page d'accueil**

- Recommandations rapides (top 3)
- Intégration native dans le dashboard

## 🧠 Algorithme de Recommandation

### 1. Construction du Profil Utilisateur

Le système analyse plusieurs sources de données :

**Préférences Explicites**

- Catégories préférées
- Niveau de difficulté souhaité
- Style d'apprentissage
- Objectifs et centres d'intérêt
- Contraintes de temps

**Historique d'Apprentissage**

- Parcours complétés par catégorie
- Niveau de difficulté moyen des parcours suivis
- Performance moyenne (progression)
- Temps moyen passé par parcours

**Données de Gamification**

- Niveau actuel et XP total
- Performance aux quiz (score moyen)
- Badges obtenus
- Défis complétés

### 2. Calcul du Score de Recommandation

Le score final (0-100) est calculé avec une pondération :

```
Score Total =
  scoreCategorie × 0.25 +      // 25% - Correspondance catégorie/intérêts
  scoreDifficulte × 0.20 +     // 20% - Niveau de difficulté approprié
  scoreDuree × 0.15 +          // 15% - Durée compatible
  scorePopularite × 0.15 +     // 15% - Popularité du parcours
  scorePerformance × 0.15 +    // 15% - Performance moyenne des autres
  scorePrerequisMatch × 0.10   // 10% - Correspondance des prérequis
```

**Bonus supplémentaires :**

- +5 points si certificat important pour l'utilisateur et disponible
- Ajustements basés sur l'historique personnel

### 3. Scores Détaillés

#### Score Catégorie (0-50 points)

- +30 points : Catégorie dans les préférences explicites
- +20 points : Catégorie dans l'historique d'apprentissage
- +15 points : Correspondance avec les centres d'intérêt

#### Score Difficulté (0-50 points)

- Correspondance exacte avec préférence : 50 points
- Différence de 1 niveau : 35 points
- Différence de 2 niveaux : 20 points
- +10 points bonus si proche de l'historique

#### Score Durée (0-50 points)

- Dans la fourchette préférée : 50 points
- Ajustement proportionnel si hors fourchette
- Bonus basé sur la disponibilité temps/semaine

#### Score Popularité (0-50 points)

- Basé sur le nombre d'inscriptions (logarithmique)
- Bonus pour taux de completion élevé

#### Score Performance (0-50 points)

- Basé sur la progression moyenne des autres apprenants

#### Score Prérequis (0-50 points)

- 50 points si pas de prérequis
- Ajustements basés sur le niveau et l'expérience

### 4. Génération des Raisons

Le système génère automatiquement les raisons de recommandation :

- "Correspond à vos centres d'intérêt"
- "Niveau de difficulté adapté à votre profil"
- "Durée compatible avec votre disponibilité"
- "Parcours populaire avec un bon taux de réussite"
- "Basé sur vos parcours précédents"
- "Certificat disponible à la fin"
- "Récompenses XP importantes"

### 5. Niveau de Correspondance

- **PARFAIT** : Score ≥ 80
- **BON** : Score ≥ 60
- **ACCEPTABLE** : Score ≥ 40
- **FAIBLE** : Score < 40

## 🎨 Interface Utilisateur

### Page Recommandations (/recommandations)

**Onglet Recommandations Personnalisées**

- Affichage des parcours avec scores IA
- Badges de correspondance colorés
- Raisons détaillées pour chaque recommandation
- Actions directes (voir, s'inscrire)

**Onglet Recherche par Critères**

- Filtres avancés : catégories, difficulté, durée, style d'apprentissage
- Sélection multiple des objectifs et centres d'intérêt
- Recherche en temps réel
- Résultats triés par pertinence

**Onglet Mes Préférences**

- Configuration complète du profil d'apprentissage
- Sauvegarde automatique
- Impact immédiat sur les recommandations

### Widget Page d'Accueil

- Top 3 des recommandations rapides
- Design intégré au dashboard
- Lien vers la page complète
- Mise à jour automatique

### Cartes de Recommandation

Chaque recommandation affiche :

- **Score IA** : Pourcentage de correspondance
- **Badge de niveau** : PARFAIT, BON, ACCEPTABLE
- **Métadonnées** : Durée, inscriptions, XP, certificat
- **Raisons** : Pourquoi ce parcours est recommandé
- **Actions** : Voir détails, s'inscrire

## 🔧 Configuration et Déploiement

### Base de Données

1. **Exécuter la migration :**

```sql
-- Créer la table des préférences
source backend/migration_add_user_preferences.sql;
```

2. **Tester le système :**

```sql
-- Exécuter les tests
source backend/test_recommendations_system.sql;
```

### Backend

Les nouveaux endpoints sont automatiquement disponibles :

- `/api/recommendations/personalized`
- `/api/recommendations/by-criteria`
- `/api/recommendations/preferences`
- `/api/recommendations/quick`

### Frontend

Le composant est déjà intégré dans les routes :

- Route : `/recommandations`
- Lien dans la navbar pour les étudiants
- Widget sur la page d'accueil

## 📊 Métriques et Analytics

### Métriques de Performance

**Précision des Recommandations**

- Taux de clic sur les recommandations
- Taux d'inscription suite aux recommandations
- Taux de completion des parcours recommandés

**Engagement Utilisateur**

- Temps passé sur la page recommandations
- Fréquence d'utilisation des filtres
- Mise à jour des préférences

**Qualité des Suggestions**

- Score moyen des recommandations acceptées
- Feedback utilisateur (si implémenté)
- Diversité des recommandations

### Optimisation Continue

**A/B Testing**

- Test de différents algorithmes de scoring
- Optimisation des pondérations
- Test d'interfaces utilisateur

**Machine Learning**

- Collecte des données d'interaction
- Amélioration des modèles de prédiction
- Personnalisation avancée

## 🚀 Évolutions Futures

### Phase 2 : IA Avancée

**Apprentissage Automatique**

- Modèles de collaborative filtering
- Deep learning pour l'analyse des préférences
- Prédiction des performances futures

**Recommandations Contextuelles**

- Recommandations basées sur l'heure/jour
- Adaptation selon l'appareil utilisé
- Recommandations sociales (amis, collègues)

### Phase 3 : Personnalisation Avancée

**Profils Dynamiques**

- Évolution automatique des préférences
- Détection des changements d'intérêt
- Adaptation aux objectifs de carrière

**Recommandations Multi-Critères**

- Parcours complémentaires
- Séquences d'apprentissage optimales
- Recommandations de groupes d'étude

### Phase 4 : Intelligence Collective

**Analyse Prédictive**

- Prédiction des tendances d'apprentissage
- Identification des lacunes de compétences
- Recommandations proactives

**Écosystème d'Apprentissage**

- Intégration avec des plateformes externes
- Recommandations cross-platform
- Parcours hybrides (en ligne/présentiel)

## 🛠️ Maintenance et Support

### Monitoring

**Logs Applicatifs**

- Suivi des requêtes de recommandation
- Performance des algorithmes
- Erreurs et exceptions

**Métriques Système**

- Temps de réponse des API
- Utilisation des ressources
- Taux d'erreur

### Mise à Jour des Données

**Recalcul Périodique**

- Mise à jour des scores de popularité
- Refresh des profils utilisateur
- Nettoyage des données obsolètes

**Optimisation des Performances**

- Cache des recommandations fréquentes
- Indexation des requêtes
- Optimisation des algorithmes

## 📚 Ressources Techniques

### APIs Disponibles

```typescript
// Service de recommandations
getPersonalizedRecommendations(maxResults?: number): Observable<ParcoursRecommendation[]>
getRecommendationsByCriteria(criteria: RecommendationRequest): Observable<ParcoursRecommendation[]>
getQuickRecommendations(): Observable<ParcoursRecommendation[]>
getUserPreferences(): Observable<UserPreferences>
saveUserPreferences(preferences: UserPreferences): Observable<UserPreferences>
```

### Modèles de Données

```typescript
interface ParcoursRecommendation {
  id: number;
  titre: string;
  description?: string;
  categorie?: string;
  niveauDifficulte?: NiveauDifficulte;
  dureeEstimeeHeures?: number;
  pointsBonus?: number;
  certificatEnabled?: boolean;
  formateurNom: string;
  nombreEtapes: number;
  nombreInscriptions: number;

  // Données IA
  scoreRecommendation: number;
  raisonsRecommandation: string[];
  niveauCorrespondance: string;

  // Scores détaillés
  scoreCategorie?: number;
  scoreDifficulte?: number;
  scoreDuree?: number;
  scorePopularite?: number;
  scorePerformance?: number;
  scorePrerequisMatch?: number;
}
```

### Configuration

```java
// Pondérations des scores (modifiables)
private static final double WEIGHT_CATEGORY = 0.25;
private static final double WEIGHT_DIFFICULTY = 0.20;
private static final double WEIGHT_DURATION = 0.15;
private static final double WEIGHT_POPULARITY = 0.15;
private static final double WEIGHT_PERFORMANCE = 0.15;
private static final double WEIGHT_PREREQUISITES = 0.10;

// Seuils de correspondance
private static final double PERFECT_MATCH_THRESHOLD = 80.0;
private static final double GOOD_MATCH_THRESHOLD = 60.0;
private static final double ACCEPTABLE_MATCH_THRESHOLD = 40.0;
```

---

**Le système de recommandations IA de 9awi Niveau transforme l'expérience d'apprentissage en proposant des parcours personnalisés et pertinents, augmentant ainsi l'engagement et la réussite des apprenants.** 🎓✨
