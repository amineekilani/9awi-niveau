# 🎬 Workflow Complet - Exemple Pas à Pas

## Scenario: Un nouvel utilisateur s'inscrit avec une photo et modifie son profil

---

## 📍 ÉTAPE 1: L'utilisateur va sur la page d'inscription

```
Navigateur: http://localhost:4200/register
```

### Ce qu'il voit:

```
┌─────────────────────────────────┐
│    Créer un compte               │
├─────────────────────────────────┤
│ Prénom: [_______________]        │
│ Nom:    [_______________]        │
│ Date:   [_______________]        │
│ Email:  [_______________]        │
│ Mdp:    [_______________]        │
│ Confirm:[_______________]        │
│                                  │
│ Photo de profil (optionnel)      │
│ ┌──────────────────────────────┐ │
│ │ 📷 Clic ou glissez une image │ │
│ │ PNG, JPG, GIF (max 10MB)    │ │
│ └──────────────────────────────┘ │
│                                  │
│        [S'inscrire]              │
└─────────────────────────────────┘
```

---

## 📸 ÉTAPE 2: L'utilisateur sélectionne une image

```typescript
// register.ts - Événement triggered
onProfileImageSelected(event: Event) {
  const file = event.target.files[0]; // photo.jpg (2MB)

  // ✅ Validation
  if (file.size > 10 * 1024 * 1024) {
    error = "Fichier trop volumineux"; // ❌ Si > 10MB
    return;
  }

  // ✅ Stockage
  this.selectedImageFile = file;

  // ✅ Prévisualisation
  const reader = new FileReader();
  reader.onload = (e) => {
    this.profileImagePreview = e.target.result; // Base64
  };
  reader.readAsDataURL(file);
}
```

### Ce qu'il voit maintenant:

```
┌─────────────────────────────────┐
│ Photo de profil (optionnel)      │
│ ┌──────────────────────────────┐ │
│ │         [Photo aperçu]       │ │
│ │         photo.jpg            │ │
│ │         [Supprimer]          │ │
│ └──────────────────────────────┘ │
└─────────────────────────────────┘
```

---

## ✍️ ÉTAPE 3: L'utilisateur remplit le formulaire et clique S'inscrire

```
Email: demo@test.com
Prénom: John
Nom: Doe
Date: 1990-01-01
Mdp: MyPassword123!
Image: photo.jpg (2MB) ✅
```

```typescript
// register.ts - Méthode onSubmit()
onSubmit() {
  // 1️⃣ Validation frontend
  if (this.password !== this.confirmPassword) {
    error = "Les mots de passe ne correspondent pas";
    return;
  }

  // 2️⃣ Envoyer données d'enregistrement
  this.authService.register({
    email: "demo@test.com",
    password: "MyPassword123!",
    firstName: "John",
    lastName: "Doe",
    dateOfBirth: "1990-01-01"
  }).subscribe({
    next: () => {
      // 3️⃣ Si image sélectionnée, l'uploader
      if (this.selectedImageFile) {
        this.uploadProfileImage();
      } else {
        redirectToLogin();
      }
    }
  });
}
```

---

## 🔵 ÉTAPE 4: Backend reçoit l'enregistrement

```bash
POST /api/auth/register HTTP/1.1
Content-Type: application/json

{
  "email": "demo@test.com",
  "password": "MyPassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01"
}
```

```java
// AuthController.java
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {

  // ✅ Créer l'utilisateur
  User user = new User();
  user.setEmail("demo@test.com");
  user.setPassword(encoder.encode("MyPassword123!"));
  user.setFirstName("John");
  user.setLastName("Doe");
  user.setDateOfBirth("1990-01-01");
  user.setProvider("local");

  // ✅ Sauvegarder en BD
  userRepository.save(user); // ID = 1

  // ✅ Envoyer email de vérification
  emailService.sendVerificationEmail(...);

  return ResponseEntity.ok(new MessageResponse("User registered successfully"));
}
```

### État de la BD maintenant:

```sql
INSERT INTO users VALUES (
  1,
  'demo@test.com',
  '$2a$10$...',  -- Password hashé
  'John',
  'Doe',
  '1990-01-01',
  NULL,  -- profileImage vide
  'local',
  ...
);
```

---

## 📤 ÉTAPE 5: Frontend upload l'image du profil

```typescript
// register.ts - Méthode uploadProfileImage()
uploadProfileImage() {
  const formData = new FormData();
  formData.append('file', this.selectedImageFile); // photo.jpg

  this.authService.uploadProfileImage(formData)
    .subscribe({
      next: (response) => {
        console.log('Image uploaded:', response.filename);
        // Rediriger vers login
        this.router.navigate(['/login']);
      }
    });
}
```

```typescript
// auth.ts - Service
uploadProfileImage(file: File): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);

  // ⚠️ Attention: Pas encore connecté!
  // Cet appel public ne nécessite pas d'authentification pour l'enregistrement
  return this.http.post(
    'http://localhost:8080/api/profile/upload-image',
    formData
  );
}
```

---

## 🟢 ÉTAPE 6: Backend reçoit l'image

```bash
POST /api/profile/upload-image HTTP/1.1
Content-Type: multipart/form-data

--boundary123
Content-Disposition: form-data; name="file"; filename="photo.jpg"
Content-Type: image/jpeg

[BINARY DATA DE L'IMAGE]
--boundary123--
```

```java
// ProfileController.java
@PostMapping("/upload-image")
public ResponseEntity<?> uploadProfileImage(
    @RequestParam("file") MultipartFile file,
    Authentication authentication) {

  try {
    // ✅ Récupérer l'utilisateur
    User user = userRepository.findByEmail("demo@test.com");

    if (file.isEmpty()) {
      return error("File is empty");
    }

    // ✅ Service: Sauvegarder l'image
    String filename = imageUploadService.saveProfileImage(file);
    // filename = "550e8400-e29b-41d4-a716-446655440000.jpg"

    // ✅ Mettre à jour l'utilisateur
    user.setProfileImage(filename);
    userRepository.save(user);

    return ResponseEntity.ok(new ImageUploadResponse(
      filename,
      "/images/users/550e8400-e29b-41d4-a716-446655440000.jpg"
    ));
  } catch (Exception e) {
    return error(e.getMessage());
  }
}
```

### Service: Qu'est-ce que saveProfileImage fait?

```java
// ImageUploadService.java
public String saveProfileImage(MultipartFile file) throws IOException {

  // 1️⃣ Validation du type
  String contentType = file.getContentType();
  if (!contentType.startsWith("image/")) {
    throw new IllegalArgumentException("Format invalide");
  }
  // ✅ "image/jpeg" → OK

  // 2️⃣ Validation de la taille
  if (file.getSize() > 10 * 1024 * 1024) { // 10MB
    throw new IllegalArgumentException("Fichier trop volumineux");
  }
  // ✅ 2MB → OK

  // 3️⃣ Créer le répertoire
  String userUploadDir = "uploads" + File.separator + "users";
  Path uploadPath = Paths.get(userUploadDir);
  if (!Files.exists(uploadPath)) {
    Files.createDirectories(uploadPath);
  }
  // ✅ uploads/users/ créé

  // 4️⃣ Générer un nom unique
  String filename = UUID.randomUUID() + ".jpg";
  // filename = "550e8400-e29b-41d4-a716-446655440000.jpg"

  // 5️⃣ Sauvegarder le fichier
  Path filePath = uploadPath.resolve(filename);
  Files.write(filePath, file.getBytes());
  // ✅ Fichier créé: uploads/users/550e8400-e29b-41d4-a716-446655440000.jpg

  return filename;
}
```

### État du système maintenant:

```
Filesystem:
  uploads/users/550e8400-e29b-41d4-a716-446655440000.jpg (2MB) ✅

Database:
  UPDATE users SET profile_image = '550e8400-e29b-41d4-a716-446655440000.jpg'
  WHERE id = 1; ✅
```

---

## ✅ ÉTAPE 7: L'utilisateur vérifie son email

```
Email reçu:
  "Veuillez cliquer ici pour vérifier votre email:
   http://localhost:8080/api/auth/verify-email?token=xyz123..."
```

```bash
GET /api/auth/verify-email?token=xyz123... HTTP/1.1
```

```java
// AuthController.java
@GetMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestParam String token) {
  User user = userRepository.findByVerificationToken(token);

  user.setEmailVerified(true);
  user.setVerificationToken(null);
  userRepository.save(user);

  return ResponseEntity.ok("Email verified!");
}
```

### État BD:

```sql
UPDATE users SET email_verified = true WHERE id = 1;
```

---

## 🔑 ÉTAPE 8: L'utilisateur se connecte

```
Navigateur: http://localhost:4200/login
Email: demo@test.com
Password: MyPassword123!
```

```bash
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
  "email": "demo@test.com",
  "password": "MyPassword123!"
}
```

```java
// AuthController.java
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

  // ✅ Vérifier credentials
  Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
      "demo@test.com",
      "MyPassword123!"
    )
  );

  // ✅ Générer JWT token
  String jwt = jwtUtils.generateJwtToken("demo@test.com");
  // jwt = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZW1vQHRlc3QuY29tIiwiaWF0IjoxNzAwNjY2MzQwLCJleHAiOjE3MDA3NTI3NDB9..."

  return ResponseEntity.ok(new JwtResponse(jwt, "demo@test.com"));
}
```

### Frontend stocke le token:

```typescript
// auth.ts - login()
next: (res: any) => {
  localStorage.setItem("auth-token", res.token); // Stocké!
  localStorage.setItem("auth-email", res.email);
  this.loggedIn.next(true);
};
```

---

## 👤 ÉTAPE 9: L'utilisateur accède au profil

```
Navigateur: http://localhost:4200/profile
```

```typescript
// profile.ts - ngOnInit()
ngOnInit() {
  this.loadProfile();
}

loadProfile() {
  const token = localStorage.getItem('auth-token');

  this.http.get('http://localhost:8080/api/profile', {
    headers: new HttpHeaders({
      'Authorization': `Bearer ${token}`
    })
  }).subscribe((profile) => {
    this.profile = profile;
  });
}
```

```bash
GET /api/profile HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

```java
// ProfileController.java
@GetMapping
public ResponseEntity<?> getProfile(Authentication authentication) {
  User user = userRepository.findByEmailAndArchivedFalse("demo@test.com");

  return ResponseEntity.ok(new ProfileResponse(
    1L,                                                    // id
    "demo@test.com",                                      // email
    "local",                                              // provider
    true,                                                 // emailVerified
    "John",                                               // firstName
    "Doe",                                                // lastName
    "1990-01-01",                                         // dateOfBirth
    "550e8400-e29b-41d4-a716-446655440000.jpg"  // profileImage ✨
  ));
}
```

### Frontend affiche:

```html
<!-- profile.html -->
<div *ngIf="profile?.profileImage">
  <img
    [src]="'http://localhost:8080/images/users/' + profile.profileImage"
    alt="Photo de profil"
    class="w-20 h-20 rounded-full"
  />
</div>
<!-- affiche: http://localhost:8080/images/users/550e8400-e29b-41d4-a716-446655440000.jpg -->
```

---

## 🖼️ ÉTAPE 10: Le navigateur récupère l'image

```bash
GET /images/users/550e8400-e29b-41d4-a716-446655440000.jpg HTTP/1.1
```

```java
// ImageUploadController.java
@GetMapping("/users/{filename}")
public ResponseEntity<?> getProfileImage(@PathVariable String filename) {

  // ✅ Sécurité: valider le nom
  if (filename.contains("..") || filename.contains("/")) {
    return ResponseEntity.badRequest().build();
  }
  // ✅ "550e8400-e29b-41d4-a716-446655440000.jpg" → OK

  // ✅ Récupérer le fichier
  Path imagePath = imageUploadService.getImagePath(filename);
  // imagePath = uploads/users/550e8400-e29b-41d4-a716-446655440000.jpg

  if (!Files.exists(imagePath)) {
    return ResponseEntity.notFound().build();
  }

  // ✅ Lire le fichier
  byte[] imageBytes = Files.readAllBytes(imagePath);

  // ✅ Déterminer le type
  String contentType = getContentType(filename);
  // contentType = "image/jpeg"

  // ✅ Retourner l'image
  return ResponseEntity.ok()
    .contentType(MediaType.parseMediaType(contentType))
    .body(imageBytes);
}
```

### Frontend affiche l'image:

```
┌─────────────────────────────────┐
│         John Doe                 │
│       [Photo affichée] ✨        │
│       demo@test.com              │
│                                  │
│    Prénom: John                  │
│    Nom: Doe                      │
│    Date: 1990-01-01              │
│                                  │
│  [Modifier le profil] [Supprimer]│
└─────────────────────────────────┘
```

---

## ✏️ ÉTAPE 11: L'utilisateur modifie sa photo

```
Clique sur "Modifier le profil"
```

### Mode édition affiche:

```
┌──────────────────────────────────┐
│  Photo actuelle:                 │
│  [Image 24x24 de la photo]       │
│                                  │
│  Zone d'upload:                  │
│  ┌────────────────────────────┐  │
│  │ 📷 Clic ou glissez image   │  │
│  │ PNG, JPG, GIF (max 10MB)  │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

```typescript
// profile.ts
onProfileImageSelected(event) {
  const file = event.target.files[0]; // nouvelle-photo.jpg

  this.selectedImageFile = file;

  // Prévisualisation
  const reader = new FileReader();
  reader.onload = (e) => {
    this.profileImagePreview = e.target.result;
  };
  reader.readAsDataURL(file);
}
```

### Utilisateur voit la prévisualisation et clique "Uploader la photo":

```typescript
// profile.ts
uploadProfileImage() {
  this.uploadingImage = true;

  this.authService.uploadProfileImage(this.selectedImageFile)
    .subscribe({
      next: () => {
        this.message = "Photo mise à jour!";
        this.uploadingImage = false;
        this.loadProfile(); // Recharge!
      }
    });
}
```

---

## 🔄 ÉTAPE 12: Backend traite le nouvel upload

```bash
POST /api/profile/upload-image HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

--boundary456
Content-Disposition: form-data; name="file"; filename="nouvelle-photo.jpg"
Content-Type: image/jpeg

[BINARY DATA DE LA NOUVELLE IMAGE]
--boundary456--
```

```java
// ProfileController.java
@PostMapping("/upload-image")
public ResponseEntity<?> uploadProfileImage(
    @RequestParam("file") MultipartFile file,
    Authentication authentication) {

  User user = userRepository.findByEmailAndArchivedFalse("demo@test.com");

  // 🔥 IMPORTANT: Supprimer l'ancienne image!
  if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
    imageUploadService.deleteProfileImage(user.getProfileImage());
    // ❌ Supprimé: uploads/users/550e8400-e29b-41d4-a716-446655440000.jpg
  }

  // Sauvegarder la nouvelle
  String newFilename = imageUploadService.saveProfileImage(file);
  // newFilename = "6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg"

  user.setProfileImage(newFilename);
  userRepository.save(user);

  return ResponseEntity.ok(new ImageUploadResponse(newFilename, "..."));
}
```

### Système de fichiers maintenant:

```
uploads/users/
  6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg ✅ (nouveau)
  # L'ancienne image est supprimée!
```

### BD:

```sql
UPDATE users SET profile_image = '6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg'
WHERE id = 1;
```

---

## 🔄 ÉTAPE 13: Frontend recharge et affiche la nouvelle image

```typescript
// profile.ts
this.loadProfile(); // Recharge!

loadProfile() {
  this.http.get('http://localhost:8080/api/profile', {...})
    .subscribe((profile) => {
      this.profile = profile;
      // profile.profileImage = "6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg"
    });
}
```

### Template affiche la nouvelle image:

```html
<img
  src="http://localhost:8080/images/users/6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg"
/>
```

---

## 🎉 RÉSULTAT FINAL

### Frontend

```
┌─────────────────────────────────┐
│      John Doe                    │
│    [NOUVELLE PHOTO] ✨           │
│    demo@test.com                 │
│                                  │
│  La photo a été mise à jour!     │
└─────────────────────────────────┘
```

### Backend

```
Filesystem:
  uploads/users/6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg ✅

Database:
  users[1].profile_image = "6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg" ✅
```

---

## 📊 Statistiques de ce workflow

| Étape | Action              | Temps  | Size |
| ----- | ------------------- | ------ | ---- |
| 1-2   | Frontend validation | ~50ms  | -    |
| 3     | Enregistrement BD   | ~100ms | -    |
| 4-5   | Upload image        | ~500ms | 2MB  |
| 6     | Sauvegarde fichier  | ~200ms | 2MB  |
| 7     | Email vérification  | ~1s    | -    |
| 8     | Login               | ~100ms | -    |
| 9-10  | Get profil          | ~50ms  | -    |
| 11    | Affichage image     | ~100ms | -    |
| 12-13 | Modification image  | ~600ms | 3MB  |

**Temps total** (sans attente email): ~2s

---

## ✅ Points Clés de ce Workflow

1. ✅ **FormData** utilisé pour l'upload
2. ✅ **UUID** généré pour le nom du fichier
3. ✅ **Validation** à chaque étape
4. ✅ **Fichier physique** sauvegardé
5. ✅ **Nom seul** en BD (pas l'image)
6. ✅ **Ancienne image** supprimée lors du changement
7. ✅ **JWT token** utilisé pour l'authentification
8. ✅ **WebConfig** sert les fichiers statiques

---

**C'est le workflow complet du système!** 🚀
