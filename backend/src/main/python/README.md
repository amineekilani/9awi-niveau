# 🤖 Agent IA de Recommandation Pédagogique - Kawi Niveau

## 📋 Vue d'ensemble

L'Agent IA de Recommandation est un système hybride intelligent qui génère des recommandations personnalisées pour chaque apprenant de la plateforme e-learning Kawi Niveau. Il combine plusieurs approches de machine learning pour maximiser la pertinence pédagogique.

## 🎯 Fonctionnalités Principales

### 🧠 Algorithmes de Recommandation

1. **Filtrage Collaboratif**

   - Utilise SVD (Singular Value Decomposition) pour la réduction dimensionnelle
   - Identifie des utilisateurs similaires basés sur leurs patterns d'apprentissage
   - Recommande des contenus appréciés par des profils similaires

2. **Filtrage Basé sur le Contenu**

   - Analyse les caractéristiques des cours (catégorie, formateur, difficulté)
   - Calcule la similarité cosinus entre les contenus
   - Recommande des cours similaires à ceux déjà suivis avec succès

3. **Recommandations par Niveau**

   - Adapte les suggestions au niveau actuel de l'utilisateur (1-10)
   - Respecte la progression pédagogique logique
   - Évite les contenus trop avancés ou trop basiques

4. **Approche Hybride**
   - Combine les trois méthodes avec pondération intelligente
   - Collaborative (40%) + Contenu (40%) + Niveau (20%)
   - Génère des explications pédagogiques personnalisées

### 🎓 Règles Pédagogiques Intégrées

- ❌ **Jamais de contenu déjà complété avec succès**
- 📉 **Score < 50% → contenu de révision/renforcement**
- 📈 **Score > 80% → contenu plus avancé**
- 🧩 **Respect de la hiérarchie : Cours → Module → Leçon → Quiz**
- 🎯 **Priorité à la motivation et progression**

## 🚀 Installation et Configuration

### Prérequis

```bash
# Python 3.8+
pip install pandas numpy scikit-learn

# Ou avec requirements.txt
pip install -r requirements.txt
```

### Structure des Fichiers

```
backend/src/main/python/
├── recommendation_engine.py    # Moteur principal
├── test_recommendations.py     # Script de test
├── requirements.txt           # Dépendances Python
└── README.md                 # Cette documentation
```

## 📊 Format des Données d'Entrée

### Enrollments DataFrame

```python
{
    'user_id': int,           # ID utilisateur
    'cours_id': int,          # ID cours
    'progress': float,        # Progression 0-100%
    'enrolled_at': int        # Timestamp inscription
}
```

### Quiz Results DataFrame

```python
{
    'user_id': int,           # ID utilisateur
    'quiz_id': int,           # ID quiz
    'score': float,           # Score 0-100%
    'date_passed': int        # Timestamp passage
}
```

### Cours DataFrame

```python
{
    'id': int,                # ID cours
    'titre': str,             # Titre du cours
    'categorie': str,         # Catégorie
    'formateur_id': int       # ID formateur
}
```

### User XP DataFrame

```python
{
    'user_id': int,           # ID utilisateur
    'total_xp': int,          # XP total
    'current_level': int      # Niveau actuel 1-10
}
```

## 🔧 Utilisation

### Exemple Basique

```python
from recommendation_engine import RecommendationEngine
import pandas as pd

# Charger vos données
enrollments_df = pd.read_sql("SELECT * FROM enrollments", connection)
quiz_results_df = pd.read_sql("SELECT * FROM resultat_quiz", connection)
cours_df = pd.read_sql("SELECT * FROM cours", connection)
user_xp_df = pd.read_sql("SELECT * FROM user_xp", connection)

# Initialiser le moteur
engine = RecommendationEngine()
engine.load_data(enrollments_df, quiz_results_df, cours_df, user_xp_df)

# Entraîner le modèle
engine.train_collaborative_filtering()

# Générer des recommandations
recommendations = engine.generate_hybrid_recommendations(user_id=1, n_recommendations=5)

print(json.dumps(recommendations, indent=2, ensure_ascii=False))
```

### Test avec Données d'Exemple

```bash
# Exécuter le script de test
cd backend/src/main/python/
python test_recommendations.py
```

## 📤 Format de Sortie JSON

```json
{
  "userId": 123,
  "generatedAt": "2024-01-15T10:30:00Z",
  "recommendations": [
    {
      "type": "COURS",
      "id": 45,
      "title": "JavaScript Avancé",
      "reason": "Recommandé par des utilisateurs similaires ayant des intérêts proches - Excellent pour continuer sur votre lancée",
      "priority": 1,
      "confidenceScore": 0.92
    }
  ]
}
```

## 🎯 Types de Recommandations

| Type        | Description          | Icône |
| ----------- | -------------------- | ----- |
| `COURS`     | Cours complets       | 📚    |
| `LECON`     | Leçons individuelles | ▶️    |
| `QUIZ`      | Quiz d'évaluation    | ❓    |
| `CHALLENGE` | Défis gamifiés       | 🏆    |

## 📈 Métriques de Performance

### Scores de Confiance

- **0.9-1.0** : Très haute confiance
- **0.7-0.9** : Haute confiance
- **0.5-0.7** : Confiance moyenne
- **0.0-0.5** : Faible confiance

### Niveaux de Priorité

1. **Priorité 1** : Critique (cours incomplets, révisions urgentes)
2. **Priorité 2** : Haute (progression logique)
3. **Priorité 3** : Moyenne (renforcement)
4. **Priorité 4** : Basse (exploration)

## 🔍 Algorithmes Détaillés

### Filtrage Collaboratif avec SVD

```python
# Normalisation de la matrice utilisateur-item
normalized_matrix = StandardScaler().fit_transform(user_item_matrix)

# Réduction dimensionnelle
svd = TruncatedSVD(n_components=50, random_state=42)
user_factors = svd.fit_transform(normalized_matrix)

# Calcul de similarité
similarity_matrix = cosine_similarity(user_factors)
```

### Calcul du Score d'Engagement

```python
def calculate_engagement_score(progress, quiz_bonus, time_bonus):
    base_score = progress / 100.0
    final_score = min(5.0, base_score * 3 + quiz_bonus + time_bonus)
    return final_score
```

### Génération d'Explications Pédagogiques

L'agent génère automatiquement des explications contextuelles :

- **Performance récente** : "Excellent pour continuer sur votre lancée"
- **Renforcement** : "Idéal pour consolider vos bases"
- **Progression** : "Parfait pour débuter votre apprentissage"
- **Similarité** : "Apprécié par des utilisateurs avec un profil similaire"

## 🚀 Intégration avec Spring Boot

### Service Java

```java
@Service
public class RecommendationService {

    public RecommendationResponse generateRecommendations(Long userId) {
        // 1. Récupérer les données utilisateur
        // 2. Appliquer les règles pédagogiques
        // 3. Enrichir avec ML (optionnel)
        // 4. Retourner les recommandations formatées
    }
}
```

### API REST

```http
GET /api/recommendations/me
GET /api/recommendations/user/{userId}
GET /api/recommendations/me/custom?maxRecommendations=10&focusArea=Programmation
```

## 🧪 Tests et Validation

### Script de Test Automatisé

```bash
python test_recommendations.py
```

**Fonctionnalités testées :**

- ✅ Génération de données réalistes
- ✅ Entraînement des modèles ML
- ✅ Recommandations par méthode
- ✅ Approche hybride
- ✅ Format JSON conforme
- ✅ Couverture des cours
- ✅ Analyse comportementale

### Métriques de Validation

- **Densité de la matrice** : Mesure la sparsité des données
- **Couverture des cours** : % de cours recommandés
- **Diversité** : Variété des catégories recommandées
- **Pertinence pédagogique** : Respect des règles d'apprentissage

## 🔧 Configuration Avancée

### Pondération des Méthodes

```python
weights = {
    'collaborative': 0.4,    # Filtrage collaboratif
    'content_based': 0.4,    # Basé sur le contenu
    'level_based': 0.2       # Basé sur le niveau
}
```

### Seuils Pédagogiques

```python
ADVANCED_THRESHOLD = 80.0    # Score pour contenu avancé
REVIEW_THRESHOLD = 50.0      # Score pour révision
MIN_QUIZZES = 2             # Minimum pour analyse
RECENT_DAYS = 7             # Activité récente
```

## 🚀 Évolutions Futures

### Fonctionnalités Prévues

1. **Deep Learning** : Réseaux de neurones pour patterns complexes
2. **Recommandations Temporelles** : Prise en compte des horaires préférés
3. **Recommandations Sociales** : Basées sur les groupes d'étude
4. **A/B Testing** : Optimisation continue des algorithmes
5. **Feedback Loop** : Apprentissage des préférences utilisateur

### Optimisations Techniques

- **Cache Redis** : Mise en cache des recommandations
- **Calcul Asynchrone** : Génération en arrière-plan
- **Batch Processing** : Traitement par lots pour performance
- **Real-time Updates** : Mise à jour en temps réel

## 📞 Support et Contribution

### Logs et Debugging

```python
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
```

### Contribution

1. Fork le projet
2. Créer une branche feature
3. Ajouter des tests
4. Soumettre une pull request

---

**🎓 Agent IA de Recommandation Pédagogique - Optimisé pour l'apprentissage personnalisé**
