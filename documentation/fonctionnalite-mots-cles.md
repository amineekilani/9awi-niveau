# Fonctionnalité Mots-clés pour les Cours

## Vue d'ensemble

Le système de mots-clés permet aux formateurs d'ajouter des tags à leurs cours pour faciliter la recherche par les apprenants.

## Fonctionnalités implémentées

### ✅ Côté Formateur (Création/Modification de cours)

1. **Ajout de mots-clés** dans le formulaire de création/modification de cours

   - Champ de saisie avec bouton "Ajouter"
   - Validation en temps réel
   - Affichage des mots-clés sous forme de badges
   - Possibilité de supprimer individuellement chaque mot-clé

2. **Sauvegarde** des mots-clés dans la base de données
   - Stockage dans le champ `keywords` de la table `cours`
   - Format : mots-clés séparés par des virgules

### ✅ Côté Apprenant (Recherche et navigation)

1. **Recherche par mots-clés**

   - Page d'accueil : recherche dans titre, description ET mots-clés
   - Page cours-list : recherche backend complète incluant les mots-clés
   - Placeholder mis à jour : "Titre, description, mots-clés..."

2. **Affichage des mots-clés**
   - Affichage des 3 premiers mots-clés sous chaque cours
   - Indicateur "+X" pour les mots-clés supplémentaires
   - Style : badges gris avec texte foncé

### ✅ Backend

1. **API REST complète**

   - Création/modification avec mots-clés
   - Recherche incluant les mots-clés
   - DTOs mis à jour (CoursRequest, CoursResponse)

2. **Base de données**
   - Colonne `keywords` VARCHAR(500) dans la table `cours`
   - Requête de recherche optimisée avec LIKE sur les mots-clés

## Comment tester

### 1. Créer un cours avec des mots-clés (Formateur)

1. Se connecter en tant que formateur
2. Aller sur "Créer un nouveau cours"
3. Remplir le formulaire et ajouter des mots-clés :
   - Taper un mot-clé (ex: "java")
   - Cliquer "Ajouter" ou appuyer sur Entrée
   - Répéter pour plusieurs mots-clés
4. Sauvegarder le cours

### 2. Rechercher par mots-clés (Apprenant)

1. Se connecter en tant qu'apprenant
2. Aller sur la page d'accueil ou la liste des cours
3. Dans la barre de recherche, taper un mot-clé (ex: "java")
4. Vérifier que les cours correspondants apparaissent
5. Vérifier que les mots-clés sont affichés sous chaque cours

### 3. Ajouter des mots-clés aux cours existants

Exécuter le script SQL fourni :

```sql
-- Voir le fichier backend/add_sample_keywords.sql
```

## Structure technique

### Frontend

- `cours-form.component.ts/html` : Gestion des mots-clés dans le formulaire
- `home.component.ts/html` : Recherche et affichage sur la page d'accueil
- `cours-list.component.ts/html` : Recherche et affichage sur la page de liste

### Backend

- `CoursController.java` : Endpoint de recherche `/api/cours/search`
- `CoursService.java` : Logique métier pour la recherche
- `CoursRepository.java` : Requête SQL avec recherche dans les mots-clés
- `CoursRequest/Response.java` : DTOs incluant le champ keywords

### Base de données

- Table `cours` : colonne `keywords VARCHAR(500)`
- Index recommandé sur la colonne keywords pour optimiser les performances

## Améliorations possibles

1. **Auto-complétion** : Suggérer des mots-clés existants lors de la saisie
2. **Nuage de mots-clés** : Page dédiée montrant tous les mots-clés populaires
3. **Filtrage par mots-clés** : Filtres dédiés en plus de la recherche textuelle
4. **Statistiques** : Analyser les mots-clés les plus recherchés
5. **Validation** : Limiter la longueur et le nombre de mots-clés par cours
