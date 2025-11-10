# Configuration Google OAuth2 pour 9awiNiveau

## 📋 Prérequis

Vous devez avoir un compte Google pour créer un projet dans Google Cloud Console.

## 🔧 Étapes de configuration

### 1. Créer un projet Google Cloud

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Cliquez sur "Sélectionner un projet" en haut
3. Cliquez sur "Nouveau projet"
4. Nommez votre projet (ex: "9awiNiveau")
5. Cliquez sur "Créer"

### 2. Activer l'API Google+

1. Dans le menu de gauche, allez dans **APIs & Services** > **Library**
2. Recherchez "Google+ API"
3. Cliquez dessus et activez-la

### 3. Configurer l'écran de consentement OAuth

1. Allez dans **APIs & Services** > **OAuth consent screen**
2. Sélectionnez **External** (ou Internal si vous avez Google Workspace)
3. Cliquez sur "Create"
4. Remplissez les informations obligatoires :
   - **App name**: 9awiNiveau
   - **User support email**: votre email
   - **Developer contact information**: votre email
5. Cliquez sur "Save and Continue"
6. Dans "Scopes", ajoutez :
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
7. Cliquez sur "Save and Continue"
8. Ajoutez des utilisateurs de test si nécessaire
9. Cliquez sur "Save and Continue"

### 4. Créer les credentials OAuth 2.0

1. Allez dans **APIs & Services** > **Credentials**
2. Cliquez sur **+ CREATE CREDENTIALS** > **OAuth client ID**
3. Sélectionnez **Web application**
4. Nommez-le (ex: "9awiNiveau Web Client")
5. Ajoutez les **Authorized JavaScript origins** :
   ```
   http://localhost:4200
   http://localhost:8080
   ```
6. Ajoutez les **Authorized redirect URIs** :
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Cliquez sur "Create"
8. **IMPORTANT** : Copiez le **Client ID** et le **Client Secret**

### 5. Configuration Backend (Spring Boot)

Ouvrez le fichier `backend/src/main/resources/application.properties` et remplacez :

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

Par vos vraies valeurs :

```properties
spring.security.oauth2.client.registration.google.client-id=123456789-abcdefghijklmnop.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-votre_secret_ici
```

### 6. Configuration Frontend (Angular)

Ouvrez les fichiers suivants et remplacez `YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com` par votre vrai Client ID :

**Fichier 1** : `frontend/src/app/login/login.ts`

```typescript
client_id: '123456789-abcdefghijklmnop.apps.googleusercontent.com',
```

**Fichier 2** : `frontend/src/app/register/register.ts`

```typescript
client_id: '123456789-abcdefghijklmnop.apps.googleusercontent.com',
```

## 🚀 Démarrage de l'application

### Backend

```bash
cd backend
mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## 🔍 Test de l'authentification

1. Ouvrez votre navigateur sur `http://localhost:4200`
2. Allez sur la page de connexion
3. Vous devriez voir un bouton "Se connecter avec Google"
4. Cliquez dessus et suivez le processus d'authentification Google
5. Vous serez redirigé vers la page d'accueil après une authentification réussie

## 📝 APIs Backend créées

### POST `/api/auth/google`

Authentifie un utilisateur avec un token Google ID.

**Request Body:**

```json
{
  "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

**Response (Success):**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "john.doe"
}
```

**Response (Error):**

```json
{
  "message": "Google authentication failed: Invalid token"
}
```

## 🔐 Sécurité

- Le mot de passe n'est pas requis pour les utilisateurs OAuth2 (champ nullable dans la base de données)
- Les utilisateurs sont identifiés par leur `provider` ("google" ou "local") et `providerId`
- Le token Google est vérifié côté backend via l'API Google
- Un JWT est généré après une authentification réussie

## 🗄️ Modifications de la base de données

La table `users` a été modifiée avec deux nouveaux champs :

- `provider` : VARCHAR (valeurs: "local" ou "google")
- `provider_id` : VARCHAR (ID unique de l'utilisateur chez le provider)

## ⚠️ Notes importantes

1. **En production**, vous devrez :

   - Ajouter votre domaine de production dans les "Authorized JavaScript origins"
   - Ajouter l'URL de callback de production dans les "Authorized redirect URIs"
   - Utiliser HTTPS obligatoirement

2. **Sécurité du Client Secret** :

   - Ne commitez JAMAIS votre client secret dans Git
   - Utilisez des variables d'environnement en production
   - Ajoutez `application.properties` dans `.gitignore` si nécessaire

3. **Limites de l'API Google** :
   - Quota gratuit : 10,000 requêtes/jour
   - Pour plus, vous devrez activer la facturation

## 🆘 Dépannage

### Erreur "redirect_uri_mismatch"

- Vérifiez que l'URI de redirection dans Google Console correspond exactement à celle utilisée

### Erreur "Invalid token"

- Vérifiez que votre Client ID est correct dans le frontend
- Assurez-vous que le token n'a pas expiré

### Le bouton Google ne s'affiche pas

- Vérifiez que le script Google est bien chargé dans `index.html`
- Ouvrez la console du navigateur pour voir les erreurs JavaScript

## 📚 Ressources

- [Google Identity Services](https://developers.google.com/identity/gsi/web)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2)
- [Google Cloud Console](https://console.cloud.google.com/)
