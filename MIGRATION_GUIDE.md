# Guide de Migration - Ajout des informations utilisateur

## Modifications apportées

### Backend

1. **Entité User** (`backend/src/main/java/com/kawi_niveau/backend/entity/User.java`)

   - Ajout des champs : `firstName`, `lastName`, `dateOfBirth`

2. **DTOs mis à jour**

   - `RegisterRequest.java` : Ajout des nouveaux champs
   - `ProfileResponse.java` : Ajout des nouveaux champs
   - `ProfileUpdateRequest.java` : Ajout des nouveaux champs

3. **Controllers mis à jour**
   - `AuthController.java` : Mise à jour de la méthode `register()` pour enregistrer les nouvelles informations
   - `ProfileController.java` : Mise à jour des méthodes `getProfile()` et `updateProfile()` pour gérer les nouveaux champs

### Frontend

1. **Page d'inscription** (`frontend/src/app/register/`)

   - Ajout des champs : Prénom, Nom, Date de naissance
   - Mise à jour du formulaire et de la logique d'envoi

2. **Page de profil** (`frontend/src/app/profile/`)

   - Affichage des nouveaux champs dans le mode lecture
   - Ajout des champs dans le mode édition
   - Mise à jour de l'interface `Profile`

3. **Service d'authentification** (`frontend/src/app/auth.ts`)
   - Mise à jour de la méthode `register()` pour inclure les nouveaux champs

### Base de données

Un script de migration SQL a été créé : `backend/migration_add_user_info.sql`

## Instructions de déploiement

### 1. Mettre à jour la base de données

Exécutez le script de migration SQL :

```bash
# Depuis le répertoire backend
mysql -u [username] -p [database_name] < migration_add_user_info.sql
```

Ou si vous utilisez PostgreSQL :

```bash
psql -U [username] -d [database_name] -f migration_add_user_info.sql
```

### 2. Redémarrer le backend

```bash
cd backend
./mvnw spring-boot:run
```

### 3. Redémarrer le frontend

```bash
cd frontend
npm start
```

## Notes importantes

- Les nouveaux champs sont **optionnels** pour les utilisateurs existants
- Les utilisateurs existants peuvent mettre à jour leur profil pour ajouter ces informations
- Les nouveaux utilisateurs devront renseigner ces champs lors de l'inscription
- La date de naissance est stockée sous forme de chaîne de caractères (format : YYYY-MM-DD)

## Compatibilité

- Les utilisateurs existants ne seront pas affectés
- Les champs peuvent être vides (NULL) dans la base de données
- L'interface affiche "Non renseigné" si les champs sont vides
