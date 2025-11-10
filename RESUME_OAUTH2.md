# 🎯 Résumé : Authentification Google OAuth2

## ✅ Ce qui a été fait

### Backend (Spring Boot)

1. **Dépendance ajoutée** dans `pom.xml` :

   - `spring-boot-starter-oauth2-client`

2. **Entité User modifiée** (`User.java`) :

   - Champ `password` maintenant nullable
   - Nouveau champ `provider` (local/google)
   - Nouveau champ `providerId` (ID Google)

3. **Nouveau DTO créé** :

   - `OAuth2LoginRequest.java` : pour recevoir le token Google

4. **Nouveau service créé** (`OAuth2Service.java`) :

   - Vérifie le token Google auprès de l'API Google
   - Crée ou récupère l'utilisateur
   - Gère la logique OAuth2

5. **Nouveau endpoint API** dans `AuthController.java` :

   - `POST /api/auth/google` : authentification avec Google

6. **Configuration ajoutée** dans `application.properties` :
   - Placeholders pour Client ID et Client Secret Google

### Frontend (Angular)

1. **Script Google ajouté** dans `index.html` :

   - Chargement de l'API Google Sign-In

2. **Service Auth étendu** (`auth.ts`) :

   - Nouvelle méthode `loginWithGoogle()`

3. **Composant Login modifié** (`login.ts` et `login.html`) :

   - Bouton "Se connecter avec Google"
   - Logique d'initialisation Google Sign-In
   - Gestion du callback Google

4. **Composant Register modifié** (`register.ts` et `register.html`) :
   - Bouton "S'inscrire avec Google"
   - Même logique que le login

## 🔑 APIs à fournir (Credentials Google)

Vous devez obtenir ces informations depuis [Google Cloud Console](https://console.cloud.google.com/) :

### 1. Google Client ID

**Format** : `123456789-abcdefghijklmnop.apps.googleusercontent.com`

**Où le mettre** :

- ✅ Backend : `backend/src/main/resources/application.properties`

  ```properties
  spring.security.oauth2.client.registration.google.client-id=VOTRE_CLIENT_ID_ICI
  ```

- ✅ Frontend : `frontend/src/app/login/login.ts` (ligne ~32)

  ```typescript
  client_id: 'VOTRE_CLIENT_ID_ICI.apps.googleusercontent.com',
  ```

- ✅ Frontend : `frontend/src/app/register/register.ts` (ligne ~60)
  ```typescript
  client_id: 'VOTRE_CLIENT_ID_ICI.apps.googleusercontent.com',
  ```

### 2. Google Client Secret

**Format** : `GOCSPX-xxxxxxxxxxxxxxxxxxxxxxxx`

**Où le mettre** :

- ✅ Backend uniquement : `backend/src/main/resources/application.properties`
  ```properties
  spring.security.oauth2.client.registration.google.client-secret=VOTRE_CLIENT_SECRET_ICI
  ```

⚠️ **IMPORTANT** : Ne partagez JAMAIS votre Client Secret publiquement !

## 📍 D'où obtenir ces credentials

### Étapes rapides :

1. **Créer un projet** sur [Google Cloud Console](https://console.cloud.google.com/)

2. **Activer l'API Google+** :

   - APIs & Services > Library > Google+ API

3. **Configurer l'écran de consentement** :

   - APIs & Services > OAuth consent screen
   - Type : External
   - Remplir les infos de base

4. **Créer les credentials** :

   - APIs & Services > Credentials
   - Create Credentials > OAuth client ID
   - Type : Web application
   - Authorized JavaScript origins : `http://localhost:4200`
   - Authorized redirect URIs : `http://localhost:8080/login/oauth2/code/google`

5. **Copier les credentials** :
   - Client ID
   - Client Secret

📖 **Guide détaillé** : Voir le fichier `GOOGLE_OAUTH_SETUP.md`

## 🚀 Pour tester

1. Configurez vos credentials Google (voir ci-dessus)

2. Démarrez le backend :

   ```bash
   cd backend
   mvnw spring-boot:run
   ```

3. Démarrez le frontend :

   ```bash
   cd frontend
   npm start
   ```

4. Ouvrez `http://localhost:4200/login`

5. Cliquez sur le bouton Google et testez !

## 📊 Architecture du flux OAuth2

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Frontend  │         │   Backend    │         │   Google    │
│  (Angular)  │         │ (Spring Boot)│         │    OAuth    │
└──────┬──────┘         └──────┬───────┘         └──────┬──────┘
       │                       │                        │
       │ 1. Click Google btn   │                        │
       ├──────────────────────>│                        │
       │                       │                        │
       │ 2. Google popup       │                        │
       ├───────────────────────┼───────────────────────>│
       │                       │                        │
       │ 3. User authenticates │                        │
       │<──────────────────────┼────────────────────────┤
       │                       │                        │
       │ 4. Send Google token  │                        │
       ├──────────────────────>│                        │
       │                       │                        │
       │                       │ 5. Verify token        │
       │                       ├───────────────────────>│
       │                       │                        │
       │                       │ 6. User info           │
       │                       │<───────────────────────┤
       │                       │                        │
       │                       │ 7. Create/Get user     │
       │                       │    Generate JWT        │
       │                       │                        │
       │ 8. Return JWT         │                        │
       │<──────────────────────┤                        │
       │                       │                        │
       │ 9. Navigate to /home  │                        │
       │                       │                        │
```

## 🔒 Sécurité

- ✅ Token Google vérifié côté backend
- ✅ JWT généré après vérification
- ✅ Pas de mot de passe stocké pour les utilisateurs OAuth2
- ✅ Provider et providerId pour identifier la source d'authentification

## 📝 Checklist finale

- [ ] Créer un projet Google Cloud
- [ ] Obtenir Client ID et Client Secret
- [ ] Configurer `application.properties` (backend)
- [ ] Configurer `login.ts` (frontend)
- [ ] Configurer `register.ts` (frontend)
- [ ] Tester l'authentification Google
- [ ] Vérifier que l'utilisateur est créé dans la base de données

Bonne chance ! 🚀
