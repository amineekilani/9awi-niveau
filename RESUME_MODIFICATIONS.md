# Résumé des modifications - Système de gestion d'images de profil

## 📋 Fichiers modifiés et créés

### Backend

#### Entités

- **User.java** (MODIFIÉ)
  - Ajout: `private String profileImage;`

#### Services

- **ImageUploadService.java** (CRÉÉ)
  - Service complet de gestion d'upload/suppression d'images
  - Validation du type et de la taille
  - Gestion des répertoires

#### Contrôleurs

- **ImageUploadController.java** (CRÉÉ)
  - `POST /images/users/upload` - Upload une image
  - `GET /images/users/{filename}` - Récupère une image
- **ProfileController.java** (MODIFIÉ)
  - Injection de `ImageUploadService`
  - `POST /api/profile/upload-image` - Upload image de profil (authentifié)
  - `GET /api/profile` - Retourne maintenant `profileImage`
  - `DELETE /api/profile/confirm-delete` - Supprime l'image lors de la suppression du compte

#### DTOs

- **ProfileResponse.java** (MODIFIÉ)
  - Ajout: `private String profileImage;`

#### Migrations

- **V3\_\_add_profile_image.sql** (CRÉÉ)
  - Migration Flyway pour ajouter la colonne `profile_image`

#### Configuration

- **application.properties** (MODIFIÉ)

  - Ajout: `upload.dir=uploads`
  - Ajout: `spring.servlet.multipart.max-file-size=10MB`
  - Ajout: `spring.servlet.multipart.max-request-size=10MB`

- **WebConfig.java** (CRÉÉ)
  - Configuration pour servir les fichiers statiques depuis `/images/**`

### Frontend

#### Services

- **auth.ts** (MODIFIÉ)
  - Nouvelle méthode: `uploadProfileImage(file: File): Observable<any>`
  - Nouvelle méthode privée: `getAuthHeaders()`

#### Components

- **RegisterComponent** (register.ts) (MODIFIÉ)

  - Propriétés: `selectedImageFile`, `profileImagePreview`
  - Méthodes: `onProfileImageSelected()`, `clearProfileImage()`, `uploadProfileImage()`
  - Upload d'image après l'inscription (optionnel)

- **ProfileComponent** (profile.ts) (MODIFIÉ)
  - Interface Profile: ajout de `profileImage?: string;`
  - Propriétés: `selectedImageFile`, `profileImagePreview`, `uploadingImage`
  - Méthodes: `onProfileImageSelected()`, `clearProfileImage()`, `uploadProfileImage()`

#### Templates

- **register.html** (MODIFIÉ)

  - Zone d'upload d'image avec prévisualisation
  - Upload optionnel pendant l'inscription

- **profile.html** (MODIFIÉ)
  - Mode affichage: Affiche la photo ou icône par défaut
  - Mode édition: Interface complète pour changer la photo avec aperçu

## 🚀 Fonctionnalités implémentées

### Lors de l'inscription

- ✅ Optionnel: Sélection d'une image de profil
- ✅ Prévisualisation de l'image
- ✅ Upload après la création du compte
- ✅ Validation de la taille (10MB max)

### Dans le profil utilisateur

- ✅ Affichage de la photo actuelle ou icône par défaut
- ✅ Mode édition: Changement de photo
- ✅ Drag & drop ou clic pour sélectionner
- ✅ Prévisualisation avant upload
- ✅ Indicateur de chargement
- ✅ Suppression automatique de l'ancienne image

### Sécurité

- ✅ Validation du type MIME
- ✅ Limite de taille
- ✅ Protection contre path traversal
- ✅ UUID pour les noms de fichiers
- ✅ Authentification requise pour profil
- ✅ Suppression lors du suppression du compte

## 📁 Structure des fichiers créés

```
backend/src/main/java/com/kawi_niveau/backend/
├── config/
│   └── WebConfig.java (CRÉÉ)
├── controller/
│   ├── ImageUploadController.java (CRÉÉ)
│   └── ProfileController.java (MODIFIÉ)
├── entity/
│   └── User.java (MODIFIÉ)
└── service/
    └── ImageUploadService.java (CRÉÉ)

backend/src/main/resources/
├── application.properties (MODIFIÉ)
└── db/migration/
    └── V3__add_profile_image.sql (CRÉÉ)

frontend/src/app/
├── auth.ts (MODIFIÉ)
├── register/
│   ├── register.html (MODIFIÉ)
│   └── register.ts (MODIFIÉ)
└── profile/
    ├── profile.html (MODIFIÉ)
    └── profile.ts (MODIFIÉ)
```

## 🔄 Flux de données

### Upload during registration

```
Register Form
    ↓
User clicks image input
    ↓
File selected & preview shown
    ↓
Form submitted
    ↓
User created in DB
    ↓
Image uploaded to /api/profile/upload-image
    ↓
File saved to uploads/users/{uuid}.{ext}
    ↓
Filename stored in DB
```

### Fetching profile

```
Client requests GET /api/profile
    ↓
Backend returns ProfileResponse with profileImage
    ↓
Frontend displays image from http://localhost:8080/images/users/{profileImage}
```

### Updating profile image

```
Edit mode activated
    ↓
User selects new image
    ↓
Preview shown
    ↓
Upload button clicked
    ↓
POST /api/profile/upload-image with FormData
    ↓
Old image deleted
    ↓
New image saved
    ↓
DB updated
    ↓
Page reloaded with new image
```

## 🎯 Points clés à retenir

1. **Les images ne sont PAS stockées dans la BD**

   - Seul le nom du fichier est sauvegardé
   - Les fichiers réels sont dans `uploads/users/`

2. **UUID pour les noms**

   - Évite les collisions
   - Sécurise les noms de fichiers

3. **URLs pour accéder aux images**

   - Format: `http://localhost:8080/images/users/{filename}`
   - Route configurée dans `WebConfig.java`

4. **Gestion des anciennes images**

   - Supprimées automatiquement lors de la mise à jour
   - Supprimées lors de la suppression du compte

5. **Optionnel à l'inscription**
   - L'upload d'image n'empêche pas l'enregistrement
   - Les erreurs d'upload ne bloquent pas le flux

## ⚙️ Configuration

### Limites de taille (configurable dans application.properties)

```properties
spring.servlet.multipart.max-file-size=10MB  # Changez le chiffre pour augmenter
spring.servlet.multipart.max-request-size=10MB
```

### Répertoire d'upload

```properties
upload.dir=uploads  # Chemin relatif au répertoire racine du projet
```

## 🧪 Tests manuels

1. **Inscription avec image**

   - Allez sur `/register`
   - Remplissez le formulaire
   - Sélectionnez une image
   - Vérifiez la prévisualisation
   - Soumettez
   - Vérifiez que le fichier existe dans `uploads/users/`

2. **Profil - Affichage**

   - Allez sur `/profile`
   - Vérifiez l'affichage de la photo ou icône
   - Vérifiez l'URL de l'image dans les DevTools

3. **Profil - Modification**
   - Cliquez "Modifier le profil"
   - Changez la photo
   - Cliquez "Uploader la photo"
   - Vérifiez que l'ancienne image est supprimée
   - Vérifiez que la nouvelle s'affiche

## 📚 Documentation complète

Voir `IMAGE_UPLOAD_DOCUMENTATION.md` pour plus de détails sur:

- Architecture complète
- Endpoints API détaillés
- Sécurité
- Troubleshooting
- Améliorations futures
