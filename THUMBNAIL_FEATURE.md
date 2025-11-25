# Fonctionnalité Thumbnail pour les Cours

## Résumé

Ajout de la possibilité pour les formateurs d'ajouter une image (thumbnail) à chaque cours. Cette image est affichée dans la liste des cours, le dashboard formateur et la page de détail du cours.

## Modifications Backend

### 1. Base de données

- **Fichier**: `backend/migration_add_thumbnail_cours.sql`
- Ajout de la colonne `thumbnail_url` (VARCHAR 500) à la table `cours`

### 2. Entité Cours

- **Fichier**: `backend/src/main/java/com/kawi_niveau/backend/entity/Cours.java`
- Ajout du champ `thumbnailUrl`

### 3. DTOs

- **CoursRequest**: Ajout du champ `thumbnailUrl`
- **CoursResponse**: Ajout du champ `thumbnailUrl`

### 4. Service

- **ImageUploadService**:
  - Ajout de `saveCoursThumbnail()` pour sauvegarder les thumbnails
  - Ajout de `getCoursThumbnailPath()` pour récupérer le chemin
  - Ajout de `deleteCoursThumbnail()` pour supprimer les thumbnails

### 5. Contrôleur

- **ImageUploadController**:
  - `POST /images/cours/upload` - Upload d'un thumbnail
  - `GET /images/cours/{filename}` - Récupération d'un thumbnail

### 6. Dossier uploads

- Création du dossier `backend/uploads/cours/` pour stocker les thumbnails

## Modifications Frontend

### 1. Service

- **cours.service.ts**:
  - Ajout du champ `thumbnailUrl` à l'interface `Cours`
  - Ajout de la méthode `uploadThumbnail(file: File)`

### 2. Formulaire de cours

- **cours-form.ts**:
  - Gestion de l'upload du fichier
  - Prévisualisation de l'image
  - Validation (type image, taille max 5MB)
- **cours-form.html**:
  - Zone de drag & drop pour l'upload
  - Prévisualisation de l'image
  - Bouton pour supprimer l'image

### 3. Liste des cours

- **cours-list.html**: Affichage du thumbnail dans les cartes de cours

### 4. Détail du cours

- **cours-detail.html**: Affichage du thumbnail en grand format en haut de la page

### 5. Dashboard formateur

- **formateur-dashboard.html**: Affichage du thumbnail dans les cartes de cours

## Utilisation

### Pour le formateur:

1. Lors de la création/modification d'un cours, cliquer sur la zone d'upload
2. Sélectionner une image (PNG, JPG, GIF, max 5MB)
3. L'image est prévisualisée immédiatement
4. Enregistrer le cours pour uploader l'image

### Pour l'étudiant:

- Les thumbnails sont automatiquement affichés dans:
  - La liste des cours disponibles
  - La page de détail du cours

## Migration de la base de données

Exécuter le script SQL:

```sql
ALTER TABLE cours ADD COLUMN thumbnail_url VARCHAR(500);
```

## Notes techniques

- Les images sont stockées dans `backend/uploads/cours/`
- Les noms de fichiers sont générés avec UUID pour éviter les conflits
- Validation côté backend: uniquement les images sont acceptées
- Validation côté frontend: taille max 5MB
- Fallback: si pas de thumbnail, une icône par défaut est affichée
