# Documentation - Système de Rôles et Gestion des Cours

## Vue d'ensemble

Ce système implémente deux rôles utilisateur (Étudiant et Formateur) avec une interface de gestion des cours pour les formateurs.

## Fonctionnalités

### 1. Système de Rôles

#### Rôles disponibles

- **ETUDIANT** (par défaut) : Peut consulter les cours disponibles
- **FORMATEUR** : Peut créer, modifier et archiver des cours

#### Création de compte

- Tous les nouveaux comptes sont créés avec le rôle **ETUDIANT** par défaut
- Les utilisateurs peuvent changer leur rôle depuis leur profil

### 2. Interface Formateur

#### Dashboard Formateur (`/formateur-dashboard`)

- Liste de tous les cours créés par le formateur
- Bouton pour ajouter un nouveau cours
- Actions : Modifier ou Archiver un cours
- Accessible uniquement aux utilisateurs avec le rôle FORMATEUR

#### Gestion des Cours

- **Créer un cours** (`/cours/nouveau`)
  - Titre (obligatoire)
  - Description (optionnelle)
- **Modifier un cours** (`/cours/modifier/:id`)
  - Seul le formateur propriétaire peut modifier son cours
- **Archiver un cours**
  - Les cours archivés ne sont plus visibles dans la liste publique
  - Le formateur conserve l'accès à ses cours archivés

### 3. Interface Étudiant

#### Liste des Cours (`/cours`)

- Affiche tous les cours non archivés
- Affiche le nom du formateur pour chaque cours
- Accessible à tous les utilisateurs connectés

### 4. Changement de Rôle

Les utilisateurs peuvent changer leur rôle depuis leur profil :

1. Aller dans **Profil**
2. Section "Changer de rôle"
3. Sélectionner le nouveau rôle (ETUDIANT ou FORMATEUR)
4. Cliquer sur "Changer de rôle"
5. Redirection automatique vers l'interface appropriée

## Structure Backend

### Entités

#### Role (Enum)

```java
public enum Role {
    ETUDIANT,
    FORMATEUR
}
```

#### Cours

```java
- id: Long
- titre: String
- description: String
- createdAt: Long
- updatedAt: Long
- archived: boolean
- archivedAt: Long
- formateur: User (ManyToOne)
```

### Endpoints API

#### Cours (`/api/cours`)

- `POST /api/cours` - Créer un cours (FORMATEUR uniquement)
- `PUT /api/cours/{id}` - Modifier un cours (propriétaire uniquement)
- `PUT /api/cours/{id}/archive` - Archiver un cours (propriétaire uniquement)
- `GET /api/cours/mes-cours` - Liste des cours du formateur connecté
- `GET /api/cours` - Liste de tous les cours non archivés
- `GET /api/cours/{id}` - Détails d'un cours

#### Profil (`/api/profile`)

- `PUT /api/profile/change-role` - Changer le rôle de l'utilisateur

## Structure Frontend

### Composants

1. **FormateurDashboardComponent** (`/formateur-dashboard`)

   - Dashboard principal du formateur
   - Liste et gestion des cours

2. **CoursFormComponent** (`/cours/nouveau`, `/cours/modifier/:id`)

   - Formulaire de création/modification de cours
   - Validation des champs

3. **CoursListComponent** (`/cours`)
   - Liste publique des cours
   - Accessible à tous les utilisateurs

### Services

#### CoursService

```typescript
- createCours(cours: Cours): Observable<Cours>
- updateCours(id: number, cours: Cours): Observable<Cours>
- archiveCours(id: number): Observable<any>
- getMesCours(): Observable<Cours[]>
- getAllCours(): Observable<Cours[]>
- getCoursById(id: number): Observable<Cours>
```

#### AuthService (mis à jour)

```typescript
- getRole(): string | null
- isFormateur(): boolean
```

## Migration Base de Données

Exécuter le script SQL suivant pour mettre à jour la base de données :

```sql
-- Fichier: backend/migration_add_role.sql

-- 1. Modifier la colonne role
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ETUDIANT';

-- 2. Mettre à jour les valeurs existantes
UPDATE users SET role = 'ETUDIANT' WHERE role = 'USER' OR role IS NULL;

-- 3. Créer la table cours
CREATE TABLE IF NOT EXISTS cours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    created_at BIGINT,
    updated_at BIGINT,
    archived BOOLEAN DEFAULT FALSE,
    archived_at BIGINT,
    formateur_id BIGINT NOT NULL,
    FOREIGN KEY (formateur_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Créer des index
CREATE INDEX idx_cours_formateur ON cours(formateur_id);
CREATE INDEX idx_cours_archived ON cours(archived);
```

## Flux Utilisateur

### Pour un Étudiant

1. Inscription → Rôle ETUDIANT par défaut
2. Connexion → Redirection vers `/cours`
3. Consultation de la liste des cours disponibles

### Pour un Formateur

1. Inscription → Rôle ETUDIANT par défaut
2. Changement de rôle vers FORMATEUR dans le profil
3. Connexion → Redirection vers `/formateur-dashboard`
4. Création et gestion des cours

## Sécurité

- Les endpoints de création/modification/archivage de cours vérifient que l'utilisateur est un FORMATEUR
- Un formateur ne peut modifier/archiver que ses propres cours
- Les tokens JWT incluent maintenant le rôle de l'utilisateur
- Le rôle est stocké dans le localStorage côté frontend

## Tests

### Tester le système

1. Créer un compte (rôle ETUDIANT par défaut)
2. Se connecter et vérifier la redirection vers `/cours`
3. Aller dans le profil et changer le rôle vers FORMATEUR
4. Vérifier la redirection vers `/formateur-dashboard`
5. Créer un cours
6. Modifier le cours
7. Archiver le cours
8. Changer le rôle vers ETUDIANT et vérifier que le cours n'est plus visible

## Améliorations Futures

- Système d'inscription aux cours pour les étudiants
- Contenu de cours (vidéos, documents, quiz)
- Système de notation et commentaires
- Statistiques pour les formateurs
- Filtres et recherche de cours
- Catégories de cours
