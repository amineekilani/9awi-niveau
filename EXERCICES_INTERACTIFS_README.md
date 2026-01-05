# Système d'Exercices Interactifs

## Vue d'ensemble

Le système d'exercices interactifs permet aux formateurs de créer trois types d'exercices engageants :

1. **Texte à trous (FILL_BLANK)** - Les étudiants complètent des phrases avec des mots manquants
2. **Glisser-déposer (DRAG_DROP)** - Les étudiants glissent des éléments dans les bonnes zones
3. **Appariement (MATCHING)** - Les étudiants associent des questions à leurs réponses

## Installation

### 1. Migrations de base de données

Exécutez les migrations SQL dans l'ordre :

```sql
SOURCE backend/migration_add_exercices_interactifs.sql;
SOURCE backend/migration_add_resultat_exercice.sql;
```

### 2. Test de l'installation

Exécutez le script de test complet :

```sql
SOURCE backend/test_exercices_complet.sql;
```

## Utilisation pour les Formateurs

### Accès à l'interface

1. Connectez-vous en tant que formateur
2. Naviguez vers un module de cours
3. Scrollez jusqu'à la section "Exercice interactif"
4. Cliquez sur "Créer un exercice"

### Création d'exercices

#### Texte à trous

1. Sélectionnez "Texte à trous"
2. Écrivez votre texte en utilisant `[BLANK:réponse]` pour les espaces à remplir
3. Exemple : `Le chat [BLANK:mange] sa nourriture dans [BLANK:la cuisine].`

#### Glisser-déposer

1. Sélectionnez "Glisser-déposer"
2. Ajoutez les éléments déplaçables
3. Créez les zones de dépôt avec leurs réponses correctes

#### Appariement

1. Sélectionnez "Appariement"
2. Créez des paires question-réponse
3. Ajoutez des options de réponse (incluant la bonne réponse)

## Utilisation pour les Étudiants

### Conditions d'accès

- Être inscrit au cours
- Avoir complété toutes les leçons du module

### Interface d'exercice

1. Cliquez sur "Faire l'exercice" dans le module
2. Complétez l'exercice selon son type
3. Cliquez sur "Valider l'exercice"
4. Consultez vos résultats détaillés

## Architecture Technique

### Backend (Spring Boot)

#### Entités principales

- `Exercice` - Informations générales de l'exercice
- `ExerciceElement` - Éléments individuels (texte, blancs, zones, etc.)
- `ResultatExercice` - Résultats des tentatives des étudiants

#### Services

- `ExerciceService` - CRUD des exercices
- `ExerciceResultatService` - Gestion des résultats et scoring

#### Endpoints API

```
POST   /api/exercice/module/{moduleId}           - Créer exercice
GET    /api/exercice/module/{moduleId}           - Récupérer exercice
PUT    /api/exercice/{exerciceId}                - Modifier exercice
DELETE /api/exercice/{exerciceId}                - Supprimer exercice

POST   /api/exercice-resultats/exercice/{id}/submit  - Soumettre réponses
GET    /api/exercice-resultats/exercice/{id}/attempts - Historique tentatives
```

### Frontend (Angular)

#### Services

- `ExerciceService` - Communication API exercices
- `ExerciceResultatService` - Communication API résultats

#### Composants

- `ModuleDetailComponent` - Interface de création intégrée
- `ExerciceViewerComponent` - Interface de passage d'exercice

## Fonctionnalités

### Gamification

- Attribution automatique de 8 XP pour chaque exercice terminé
- Bonus de 5 XP pour les exercices réussis (score ≥ 60%)
- Intégration avec le système de badges existant

### Suivi des performances

- Historique des tentatives
- Meilleur score par exercice
- Résultats détaillés avec corrections
- Temps de completion

### Sécurité

- Vérification des permissions formateur
- Validation des données côté serveur
- Protection contre les soumissions multiples

## Types d'éléments

### Exercice

```typescript
interface Exercice {
  id?: number;
  titre: string;
  description?: string;
  typeExercice: "FILL_BLANK" | "DRAG_DROP" | "MATCHING";
  moduleId?: number;
  elements?: ExerciceElement[];
}
```

### Élément d'exercice

```typescript
interface ExerciceElement {
  id?: number;
  contenu: string;
  typeElement: "TEXT" | "BLANK" | "DRAGGABLE" | "DROP_ZONE" | "MATCH_ITEM";
  positionOrdre: number;
  reponseCorrecte?: string;
  options?: string[];
}
```

## Exemples d'utilisation

### Texte à trous - Grammaire

```
Le chat [BLANK:mange] sa nourriture dans [BLANK:la cuisine] tous les [BLANK:matins].
```

### Glisser-déposer - Classification

Éléments : `Chat`, `Chien`, `Aigle`, `Poisson`
Zones : `Mammifères`, `Oiseaux`, `Poissons`

### Appariement - Géographie

- France → Paris, Londres, Berlin, Madrid
- Allemagne → Paris, Londres, Berlin, Madrid
- Espagne → Paris, Londres, Berlin, Madrid

## Dépannage

### Problèmes courants

1. **Exercice non visible** - Vérifiez que toutes les leçons sont complétées
2. **Erreur de création** - Vérifiez que le titre est renseigné et qu'il y a au moins un élément
3. **Problème de drag & drop** - Assurez-vous que JavaScript est activé

### Logs utiles

- Backend : Recherchez `🎯 Attribution des récompenses` dans les logs
- Frontend : Ouvrez la console développeur pour les erreurs JavaScript

## Évolutions futures

- Support des images dans les exercices
- Exercices chronométrés
- Mode collaboratif
- Export des résultats en CSV
- Exercices adaptatifs selon le niveau

## Support

Pour toute question ou problème :

1. Vérifiez les logs d'erreur
2. Testez avec le script SQL de test
3. Consultez la documentation API
4. Contactez l'équipe de développement
