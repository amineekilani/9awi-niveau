# Fonctionnalité de Recherche et Filtrage des Cours

## Description

Cette fonctionnalité permet aux étudiants de rechercher et filtrer les cours disponibles dans la page d'accueil.

## Fonctionnalités

### 1. Recherche par nom

- Barre de recherche permettant de chercher des cours par leur titre ou description
- Recherche en temps réel (mise à jour automatique lors de la saisie)
- Recherche insensible à la casse

### 2. Filtrage par catégorie

- Menu déroulant affichant toutes les catégories disponibles
- Option "Toutes les catégories" pour afficher tous les cours
- Les catégories sont extraites automatiquement des cours existants

### 3. Combinaison des filtres

- Possibilité de combiner la recherche par nom et le filtrage par catégorie
- Les filtres s'appliquent simultanément

### 4. Réinitialisation

- Bouton "Réinitialiser" pour effacer tous les filtres
- Le bouton n'apparaît que lorsqu'au moins un filtre est actif

### 5. Affichage des résultats

- Compteur affichant le nombre de cours trouvés
- Message informatif indiquant les critères de recherche actifs
- Message spécial si aucun cours ne correspond aux critères
- Bouton pour revenir à la liste complète

## Utilisation

1. **Rechercher un cours** : Tapez dans la barre de recherche le nom ou une partie de la description du cours
2. **Filtrer par catégorie** : Sélectionnez une catégorie dans le menu déroulant
3. **Combiner les filtres** : Utilisez la recherche et le filtre de catégorie ensemble
4. **Réinitialiser** : Cliquez sur le bouton "Réinitialiser" pour effacer tous les filtres

## Emplacement des fichiers modifiés

- `frontend/src/app/cours-list/cours-list.ts` : Logique de recherche et filtrage
- `frontend/src/app/cours-list/cours-list.html` : Interface utilisateur
- `frontend/src/app/cours-list/cours-list.css` : Styles pour la barre de recherche

## Améliorations futures possibles

- Recherche avancée avec plus de critères (formateur, niveau, durée)
- Tri des résultats (par popularité, date, note)
- Sauvegarde des préférences de filtrage
- Suggestions de recherche automatiques
