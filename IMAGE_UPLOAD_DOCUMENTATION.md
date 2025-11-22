# Système de Gestion d'Images de Profil - Documentation Complète

## Vue d'ensemble

Ce système permet aux utilisateurs d'uploader et de gérer leur photo de profil lors de l'inscription et dans le profil utilisateur. Les images sont stockées physiquement sur le serveur et la base de données ne conserve que le nom du fichier.

## Architecture

### Backend (Spring Boot)

#### 1. **Entity - User.java**

Ajout d'un champ `profileImage` pour stocker le nom du fichier:

```java
@Column(name = "profile_image")
private String profileImage; // Nom du fichier image de profil
```

#### 2. **Migration Flyway - V3\_\_add_profile_image.sql**

Ajoute la colonne `profile_image` à la table `users`.

#### 3. **Service - ImageUploadService.java**

Gère l'upload et la suppression des fichiers images:

- `saveProfileImage(MultipartFile file)` : Sauvegarde une image et retourne son nom
- `deleteProfileImage(String filename)` : Supprime une image existante
- `getImagePath(String filename)` : Récupère le chemin complet du fichier

**Fonctionnalités:**

- Validation du type de fichier (images uniquement)
- Génération d'un UUID pour le nom du fichier
- Création du répertoire `uploads/users/` si nécessaire
- Limite de taille: 10MB (configurable)

#### 4. **Controller - ImageUploadController.java**

API REST pour gérer les images:

**Endpoints:**

- `POST /images/users/upload` : Upload une image

  - Paramètre: `file` (FormData)
  - Retour: `{ filename, url }`

- `GET /images/users/{filename}` : Récupère une image
  - Paramètre: nom du fichier
  - Retour: Le fichier image avec le bon Content-Type

**Sécurité:**

- Validation contre les path traversal attacks
- Vérification de l'existence du fichier
- Détermination du Content-Type basée sur l'extension

#### 5. **Controller - ProfileController.java**

Mise à jour avec la gestion des images:

**Nouvel endpoint:**

- `POST /api/profile/upload-image` : Upload l'image de l'utilisateur connecté
  - Supprime l'ancienne image si elle existe
  - Sauvegarde la nouvelle
  - Retour: `{ filename, url }`

**Modifications:**

- `GET /api/profile` : Inclut maintenant `profileImage`
- `DELETE /api/profile/confirm-delete` : Supprime l'image lors de la suppression du compte

#### 6. **DTO - ProfileResponse.java**

Ajout du champ `profileImage`:

```java
private String profileImage;
```

#### 7. **Configuration - application.properties**

```properties
# Image Upload Configuration
upload.dir=uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### 8. **Configuration - WebConfig.java**

Configure le serveur pour servir les fichiers statiques du répertoire `uploads`:

```java
registry
    .addResourceHandler("/images/**")
    .addResourceLocations(uploadsPath)
    .setCachePeriod(3600); // Cache 1 heure
```

### Frontend (Angular)

#### 1. **Service - AuthService**

Ajout de la méthode `uploadProfileImage`:

```typescript
uploadProfileImage(file: File): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);

  const headers = this.getAuthHeaders();
  return this.http.post('http://localhost:8080/api/profile/upload-image', formData, { headers });
}
```

#### 2. **Component - RegisterComponent**

Intégration de l'upload d'image:

- Champ d'input file avec aperçu
- Validation de la taille (10MB)
- Upload après l'inscription (optionnel)
- Propriétés: `selectedImageFile`, `profileImagePreview`
- Méthodes: `onProfileImageSelected()`, `clearProfileImage()`, `uploadProfileImage()`

#### 3. **Component - ProfileComponent**

Gestion complète de l'image de profil:

- **Mode affichage**: Affiche l'image ou l'icône par défaut
- **Mode édition**: Interface complète pour changer la photo

  - Aperçu de l'image actuelle
  - Zone d'upload avec glisser-déposer
  - Sélection et prévisualisation de la nouvelle image
  - Bouton d'upload avec loading
  - Option de suppression

- **Propriétés:**

  - `selectedImageFile`: Le fichier sélectionné
  - `profileImagePreview`: Prévisualisation en base64
  - `uploadingImage`: État de l'upload

- **Méthodes:**
  - `onProfileImageSelected(event)`: Gère la sélection
  - `clearProfileImage(event)`: Efface la sélection
  - `uploadProfileImage()`: Envoie au serveur

#### 4. **Template - register.html**

Ajout d'une zone d'upload d'image:

- Label "Photo de profil (optionnel)"
- Zone de dépôt en pointillés
- Prévisualisation de l'image sélectionnée
- Icône feather pour l'UX

#### 5. **Template - profile.html**

Modifications:

- **Mode affichage**: Affiche la photo actuelle ou icône par défaut
- **Mode édition**: Section complète pour gérer la photo
  - Affichage de la photo actuelle (24x24px)
  - Zone d'upload
  - Prévisualisation
  - Bouton d'upload

## Flux de travail

### Inscription avec photo

1. Utilisateur remplit le formulaire d'inscription
2. Sélectionne une image de profil (optionnel)
3. Soumet le formulaire
4. L'utilisateur est créé en BD
5. Si une image est sélectionnée, elle est uploadée
6. Email de vérification est envoyé

### Modification du profil

1. Utilisateur accède à la page profil
2. Clique sur "Modifier le profil"
3. Mode édition s'affiche avec la photo actuelle
4. Utilisateur peut:
   - Cliquer/glisser-déposer une nouvelle image
   - Voir une prévisualisation
   - Cliquer "Uploader la photo" pour valider
   - Continuer à modifier les autres champs
5. Clique "Enregistrer" pour sauvegarder

### Suppression du compte

1. Utilisateur demande la suppression du compte
2. L'image de profil est automatiquement supprimée
3. Le compte est archivé (pas vraiment supprimé)

## Structure des fichiers

```
uploads/
└── users/
    ├── {uuid}.jpg
    ├── {uuid}.png
    └── ...
```

## Endpoints API

### Upload d'image

```
POST /images/users/upload
Content-Type: multipart/form-data

file: <File>

Response:
{
  "filename": "12345-uuid.jpg",
  "url": "/images/users/12345-uuid.jpg"
}
```

### Récupérer une image

```
GET /images/users/{filename}

Response: Image binary avec Content-Type approprié
```

### Upload image du profil (authentifié)

```
POST /api/profile/upload-image
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: <File>

Response:
{
  "filename": "12345-uuid.jpg",
  "url": "/images/users/12345-uuid.jpg"
}
```

### Récupérer le profil (authentifié)

```
GET /api/profile
Authorization: Bearer {token}

Response:
{
  "id": 1,
  "email": "user@example.com",
  "provider": "local",
  "emailVerified": true,
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "profileImage": "12345-uuid.jpg"
}
```

## Validation et Sécurité

### Backend

- ✅ Validation du type MIME (images uniquement)
- ✅ Validation de la taille (10MB max)
- ✅ Protection contre path traversal attacks
- ✅ Génération d'UUID pour éviter les collisions
- ✅ Suppression de l'ancienne image lors de la mise à jour
- ✅ Suppression de l'image lors de la suppression du compte
- ✅ Authentification requise pour l'upload de profil

### Frontend

- ✅ Validation de la taille (10MB)
- ✅ Validation du type de fichier (accept="image/\*")
- ✅ Prévisualisation avant upload
- ✅ Indicateur de chargement pendant l'upload
- ✅ Gestion des erreurs avec messages utilisateur

## Configuration requise

### Backend

- Spring Boot 3.x+
- MySQL/MariaDB
- Dossier `uploads` doit être accessible en écriture
- 10MB d'espace libre minimum

### Frontend

- Angular 17+
- HttpClientModule (déjà importé)
- FormsModule (déjà importé)

## Troubleshooting

### Le dossier uploads n'est pas créé

Le dossier est créé automatiquement au premier upload. Assurez-vous que le répertoire parent est inscriptible.

### Les images ne s'affichent pas

1. Vérifiez que le serveur est lancé sur le port 8080
2. Vérifiez que les fichiers existent dans `uploads/users/`
3. Vérifiez que `WebConfig.java` est bien intégré

### Upload échoue avec erreur 413

La taille du fichier dépasse la limite. Augmentez dans `application.properties`:

```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### Erreur 401 lors de l'upload

Assurez-vous que le token JWT est inclus dans les headers.

## Améliorations futures possibles

1. Redimensionnement automatique des images
2. Compression des images
3. Stockage sur un service cloud (S3, etc.)
4. Crop/rotation de l'image avant upload
5. Galerie de photos multiples
6. Historique des modifications
