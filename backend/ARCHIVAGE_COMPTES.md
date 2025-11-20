# Archivage des comptes utilisateurs

## Vue d'ensemble

Les comptes utilisateurs ne sont plus supprimés définitivement de la base de données. Au lieu de cela, ils sont **archivés** et restent dans la base de données avec un flag `archived = true`.

## Modifications apportées

### 1. Entité User (`User.java`)

Ajout de deux nouveaux champs :

- `archived` (boolean) : Indique si le compte est archivé (par défaut : false)
- `archivedAt` (Long) : Timestamp en millisecondes de la date d'archivage

### 2. Repository (`UserRepository.java`)

- Ajout de la méthode `findByEmailAndArchivedFalse()` pour exclure les comptes archivés
- Conservation de `findByEmail()` pour usage interne

### 3. Contrôleurs mis à jour

#### ProfileController

- `getProfile()` : Utilise `findByEmailAndArchivedFalse()`
- `updateProfile()` : Utilise `findByEmailAndArchivedFalse()`
- `requestAccountDeletion()` : Utilise `findByEmailAndArchivedFalse()`
- `confirmAccountDeletion()` : Archive le compte au lieu de le supprimer
  - Définit `archived = true`
  - Enregistre `archivedAt` avec le timestamp actuel
  - Nettoie les tokens de suppression

#### AuthController

- `authenticateUser()` : Utilise `findByEmailAndArchivedFalse()`
- `registerUser()` : Utilise `findByEmailAndArchivedFalse()`
- `forgotPassword()` : Utilise `findByEmailAndArchivedFalse()`

#### OAuth2Service

- `processGoogleUser()` : Utilise `findByEmailAndArchivedFalse()`

#### UserDetailsServiceImpl

- `loadUserByUsername()` : Utilise `findByEmailAndArchivedFalse()`

### 4. Migration SQL (`migration_add_archived.sql`)

Script SQL pour ajouter les colonnes à la base de données existante :

- Ajoute la colonne `archived` (BOOLEAN, défaut FALSE)
- Ajoute la colonne `archived_at` (BIGINT)
- Crée des index pour optimiser les performances

## Comportement

### Avant

Lorsqu'un utilisateur confirmait la suppression de son compte :

- Le compte était supprimé définitivement de la base de données
- Toutes les données étaient perdues

### Après

Lorsqu'un utilisateur confirme la suppression de son compte :

- Le compte est marqué comme `archived = true`
- La date d'archivage est enregistrée dans `archivedAt`
- Les tokens de suppression sont nettoyés
- Le compte reste dans la base de données mais est invisible pour l'application

### Comptes archivés

Les comptes archivés :

- Ne peuvent pas se connecter
- N'apparaissent pas dans les recherches par email
- Ne peuvent pas réinitialiser leur mot de passe
- Ne peuvent pas s'inscrire avec le même email (l'email est toujours considéré comme pris)

## Migration

Pour appliquer les changements à une base de données existante :

```bash
# Se connecter à la base de données
mysql -u root -p votre_base_de_donnees

# Exécuter le script de migration
source migration_add_archived.sql
```

Ou avec PostgreSQL :

```bash
psql -U postgres -d votre_base_de_donnees -f migration_add_archived.sql
```

## Avantages

1. **Conformité RGPD** : Les données peuvent être conservées pour des raisons légales
2. **Récupération** : Possibilité de restaurer un compte archivé si nécessaire
3. **Audit** : Traçabilité des comptes supprimés
4. **Prévention de fraude** : Empêche la réutilisation immédiate d'un email archivé

## Fonctionnalités futures possibles

- Interface admin pour voir les comptes archivés
- Fonction de restauration de compte
- Suppression automatique des comptes archivés après X mois
- Export des données avant archivage
