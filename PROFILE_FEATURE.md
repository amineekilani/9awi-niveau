# Fonctionnalité de Gestion de Profil

## Vue d'ensemble

Cette fonctionnalité permet aux utilisateurs de gérer leur profil, modifier leurs informations et supprimer leur compte de manière sécurisée.

## Fonctionnalités

### 1. Affichage du Profil

- Affiche les informations de l'utilisateur (nom d'utilisateur, email, type de compte)
- Indique si l'email est vérifié
- Accessible via le bouton "Gérer le profil" sur la page d'accueil

### 2. Modification du Profil

- Modifier le nom d'utilisateur
- Modifier l'email (nécessite une nouvelle vérification)
- Changer le mot de passe (uniquement pour les comptes locaux)
  - Nécessite le mot de passe actuel
  - Validation de la correspondance des mots de passe
  - Minimum 6 caractères

### 3. Suppression de Compte

- Processus en deux étapes pour la sécurité :
  1. L'utilisateur demande la suppression en confirmant son email
  2. Un email de confirmation est envoyé avec un lien valide 1 heure
  3. L'utilisateur clique sur le lien pour confirmer la suppression
- Suppression définitive et irréversible de toutes les données

## Architecture

### Backend

#### Nouveaux Endpoints (ProfileController)

- `GET /api/profile` - Récupérer les informations du profil
- `PUT /api/profile` - Mettre à jour le profil
- `POST /api/profile/request-delete` - Demander la suppression du compte
- `DELETE /api/profile/confirm-delete?token=xxx` - Confirmer la suppression

#### Nouveaux DTOs

- `ProfileResponse` - Réponse contenant les données du profil
- `ProfileUpdateRequest` - Requête de mise à jour du profil
- `DeleteAccountRequest` - Requête de suppression de compte

#### Modifications de la Base de Données

- Ajout de `delete_token` dans la table `users`
- Ajout de `delete_token_expiry` dans la table `users`

#### Service Email

- Nouvelle méthode `sendAccountDeletionEmail()` pour envoyer l'email de confirmation

### Frontend

#### Nouveaux Composants

1. **ProfileComponent** (`/profile`)

   - Affichage et modification du profil
   - Gestion de la suppression de compte
   - Trois modes : affichage, édition, suppression

2. **ConfirmDeleteComponent** (`/confirm-delete`)
   - Page de confirmation de suppression
   - Traite le token de suppression
   - Redirige vers la page de connexion après suppression

#### Modifications

- Ajout d'un bouton "Gérer le profil" dans HomeComponent
- Nouvelles routes dans `app.routes.ts`
- Routes protégées par `authGuard`

## Sécurité

### Token JWT

- Toutes les requêtes API utilisent le token JWT stocké dans localStorage
- Le token est envoyé dans l'en-tête `Authorization: Bearer <token>`

### Validation

- Vérification de l'email pour la suppression de compte
- Vérification du mot de passe actuel pour le changement de mot de passe
- Tokens de suppression avec expiration (1 heure)

### Protection des Routes

- Routes frontend protégées par `authGuard`
- Routes backend protégées par Spring Security

## Utilisation

### Pour l'utilisateur

1. **Accéder au profil**

   - Cliquer sur "Gérer le profil" depuis la page d'accueil

2. **Modifier le profil**

   - Cliquer sur "Modifier le profil"
   - Modifier les champs souhaités
   - Cliquer sur "Enregistrer"

3. **Changer le mot de passe** (comptes locaux uniquement)

   - En mode édition, remplir les champs de mot de passe
   - Entrer le mot de passe actuel
   - Entrer et confirmer le nouveau mot de passe

4. **Supprimer le compte**
   - Cliquer sur "Supprimer le compte"
   - Confirmer l'email
   - Cliquer sur "Envoyer l'email de confirmation"
   - Consulter l'email et cliquer sur le lien de confirmation
   - Le compte sera supprimé définitivement

## Configuration Requise

### Backend

- Spring Boot avec Spring Security
- Service d'envoi d'emails (Brevo/SendinBlue)
- Base de données avec migration Flyway

### Frontend

- Angular avec routing
- HttpClient pour les appels API
- FormsModule pour les formulaires
- Feather Icons pour les icônes

## Notes Importantes

1. **Comptes Google OAuth**

   - Ne peuvent pas changer leur mot de passe
   - Peuvent modifier leur nom d'utilisateur et email

2. **Vérification d'email**

   - Si l'email est modifié, il doit être vérifié à nouveau
   - Le statut de vérification est affiché dans le profil

3. **Suppression de compte**

   - Action irréversible
   - Toutes les données sont supprimées
   - L'utilisateur est automatiquement déconnecté après suppression

4. **Token de suppression**
   - Valide pendant 1 heure
   - Un seul token actif à la fois
   - Expire automatiquement
