# 💻 Exemples de Code - Système d'Upload d'Images

Ce fichier contient des exemples pratiques pour utiliser le système d'upload d'images.

## 🔵 Backend (Java/Spring Boot)

### 1. Créer une image depuis Angular

```typescript
// Angular Component
onProfileImageSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];

    // Validation
    if (file.size > 10 * 1024 * 1024) {
      alert('Fichier trop volumineux!');
      return;
    }

    this.selectedFile = file;

    // Créer une prévisualisation
    const reader = new FileReader();
    reader.onload = (e) => {
      this.preview = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
}
```

### 2. Uploader l'image

```typescript
// Angular Service
uploadProfileImage(file: File): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);

  return this.http.post(
    'http://localhost:8080/api/profile/upload-image',
    formData,
    { headers: new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    })}
  );
}
```

### 3. Appeler depuis le composant

```typescript
// Angular Component
uploadImage(): void {
  if (!this.selectedFile) return;

  this.authService.uploadProfileImage(this.selectedFile)
    .subscribe({
      next: (response) => {
        console.log('Upload réussi:', response.filename);
        this.loadProfile(); // Recharge le profil
      },
      error: (error) => {
        console.error('Erreur upload:', error);
      }
    });
}
```

### 4. Récupérer le profil avec l'image

```typescript
// Angular
loadProfile(): void {
  this.http.get('http://localhost:8080/api/profile', {
    headers: new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    })
  }).subscribe((profile: any) => {
    console.log('Profile image:', profile.profileImage);
    // URL: http://localhost:8080/images/users/{profileImage}
  });
}
```

### 5. Afficher l'image dans le template

```html
<!-- Angular Template -->
<div *ngIf="profile?.profileImage">
  <img
    [src]="'http://localhost:8080/images/users/' + profile.profileImage"
    alt="Photo de profil"
    class="w-20 h-20 rounded-full"
  />
</div>
<div *ngIf="!profile?.profileImage">
  <div
    class="w-20 h-20 rounded-full bg-gray-300 flex items-center justify-center"
  >
    <i data-feather="user" class="text-gray-600"></i>
  </div>
</div>
```

## 🟢 Backend (Java/Spring Boot)

### 1. Configuration WebConfig

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadsPath = Paths.get(uploadDir)
            .toAbsolutePath()
            .toUri()
            .toString();

        registry
            .addResourceHandler("/images/**")
            .addResourceLocations(uploadsPath)
            .setCachePeriod(3600); // Cache 1h
    }
}
```

### 2. Service ImageUploadService

```java
@Service
public class ImageUploadService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    public String saveProfileImage(MultipartFile file) throws IOException {
        // Validation
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Format invalide");
        }

        // Créer le répertoire
        String userUploadDir = uploadDir + File.separator + "users";
        Path uploadPath = Paths.get(userUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer le nom
        String filename = UUID.randomUUID() + "." + getFileExtension(file.getOriginalFilename());

        // Sauvegarder
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        return filename;
    }

    public void deleteProfileImage(String filename) {
        if (filename == null || filename.isEmpty()) return;

        try {
            String userUploadDir = uploadDir + File.separator + "users";
            Path filePath = Paths.get(userUploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur suppression: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public Path getImagePath(String filename) {
        String userUploadDir = uploadDir + File.separator + "users";
        return Paths.get(userUploadDir).resolve(filename);
    }
}
```

### 3. Controller - Upload endpoint

```java
@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "http://localhost:4200")
public class ImageUploadController {

    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping("/users/upload")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Fichier vide"));
            }

            String filename = imageUploadService.saveProfileImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new MessageResponse("Erreur upload: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{filename}")
    public ResponseEntity<?> getProfileImage(@PathVariable String filename) {
        try {
            if (filename.contains("..") || filename.contains("/")) {
                return ResponseEntity.badRequest().build();
            }

            Path imagePath = imageUploadService.getImagePath(filename);

            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(imagePath);
            String contentType = getContentType(filename);

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private String getContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "image/jpeg";
        };
    }
}
```

### 4. Controller - Profile endpoint

```java
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {

    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            User user = userRepository
                .findByEmailAndArchivedFalse(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Fichier vide"));
            }

            // Supprimer l'ancienne image
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                imageUploadService.deleteProfileImage(user.getProfileImage());
            }

            // Sauvegarder la nouvelle
            String filename = imageUploadService.saveProfileImage(file);
            user.setProfileImage(filename);
            userRepository.save(user);

            return ResponseEntity.ok(new ImageUploadResponse(filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = userRepository
            .findByEmailAndArchivedFalse(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new ProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getProvider(),
            user.isEmailVerified(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getProfileImage()  // 🆕
        ));
    }
}
```

## 📱 Frontend HTML/CSS

### 1. Zone d'upload complète

```html
<div
  class="border-2 border-dashed border-gray-300 rounded-lg p-6 
            text-center hover:border-primary-500 transition-colors 
            cursor-pointer"
  (click)="fileInput.click()"
>
  <input
    #fileInput
    type="file"
    class="hidden"
    accept="image/*"
    (change)="onProfileImageSelected($event)"
    name="profileImage"
  />

  <!-- Avant sélection -->
  <div *ngIf="!profileImagePreview" class="flex flex-col items-center">
    <i data-feather="image" class="text-gray-400 mb-2" width="32"></i>
    <p class="text-sm text-gray-600">Cliquez ou glissez une image</p>
    <p class="text-xs text-gray-500">PNG, JPG, GIF (max 10MB)</p>
  </div>

  <!-- Après sélection -->
  <div *ngIf="profileImagePreview" class="flex flex-col items-center">
    <img
      [src]="profileImagePreview"
      class="h-16 w-16 object-cover rounded-lg mb-2"
    />
    <p class="text-sm text-gray-600">{{ selectedImageFile?.name }}</p>
    <p
      class="text-xs text-primary-500 cursor-pointer mt-1"
      (click)="clearProfileImage($event)"
    >
      Supprimer
    </p>
  </div>
</div>

<!-- Bouton d'upload -->
<button
  *ngIf="selectedImageFile && !uploadingImage"
  type="button"
  (click)="uploadProfileImage()"
  class="mt-4 w-full px-4 py-2 bg-primary-500 text-white 
               rounded-lg hover:bg-primary-600 transition-all"
>
  <i data-feather="upload" width="18"></i>
  Uploader la photo
</button>

<!-- Loading -->
<div *ngIf="uploadingImage" class="mt-4 flex items-center justify-center gap-2">
  <div class="animate-spin rounded-full h-5 w-5 border-b-2"></div>
  <span>Upload en cours...</span>
</div>
```

### 2. Affichage de l'image

```html
<!-- Avec image -->
<div
  *ngIf="profile?.profileImage"
  class="w-20 h-20 rounded-full overflow-hidden"
>
  <img
    [src]="'http://localhost:8080/images/users/' + profile.profileImage"
    alt="Photo de profil"
    class="w-full h-full object-cover"
  />
</div>

<!-- Sans image -->
<div
  *ngIf="!profile?.profileImage"
  class="w-20 h-20 rounded-full bg-gradient-to-br from-primary-500 
             to-primary-700 flex items-center justify-center"
>
  <i data-feather="user" class="text-white" width="40"></i>
</div>
```

## 🧪 Tests API avec cURL

### 1. Enregistrement

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "Pass123!",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01"
  }'
```

### 2. Connexion

```bash
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "Pass123!"
  }' | jq -r '.token')

echo $TOKEN
```

### 3. Upload d'image du profil

```bash
curl -X POST http://localhost:8080/api/profile/upload-image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/chemin/vers/image.jpg"
```

### 4. Récupérer le profil

```bash
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Récupérer l'image

```bash
# Directement depuis le navigateur:
http://localhost:8080/images/users/{filename}

# Ou avec curl:
curl -o mon-image.jpg \
  http://localhost:8080/images/users/550e8400-e29b-41d4-a716-446655440000.jpg
```

## 🔧 Configuration

### application.properties

```properties
# Répertoire d'upload
upload.dir=uploads

# Limite de taille de fichier
spring.servlet.multipart.max-file-size=10MB

# Limite de requête multipart
spring.servlet.multipart.max-request-size=10MB
```

## 📊 Modèle de Réponse

### Réussite

```json
{
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "url": "/images/users/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

### Erreur

```json
{
  "message": "Le fichier ne doit pas dépasser 10MB"
}
```

## 🎯 Cas d'Usage Complets

### Scénario 1: Inscription avec image

```
1. Utilisateur va sur /register
2. Remplit: prénom, nom, email, mdp, date
3. Sélectionne une image
4. Clique "S'inscrire"
5. Image uploadée automatiquement
6. Email de vérification envoyé
```

### Scénario 2: Modifier la photo

```
1. Utilisateur va sur /profile
2. Clique "Modifier le profil"
3. Zone d'upload affichée
4. Glisse une nouvelle image
5. Clique "Uploader la photo"
6. Ancienne image supprimée
7. Nouvelle image affichée
```

## 💾 Stockage des Fichiers

```
uploads/
└── users/
    ├── 550e8400-e29b-41d4-a716-446655440000.jpg
    ├── 6ba7b810-9dad-11d1-80b4-00c04fd430c8.png
    └── 7ca4a39f-5e6c-4a8b-9c1b-8f2d4e6b5a9c.gif
```

Les fichiers sont organisés par UUID pour garantir l'unicité.

---

**Vous pouvez copier-coller ces exemples directement dans votre code!**
