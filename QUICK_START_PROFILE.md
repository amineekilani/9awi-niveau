# Guide de Démarrage Rapide - Gestion de Profil

## Étapes pour tester la fonctionnalité

### 1. Backend

```bash
cd backend
./mvnw spring-boot:run
```

La migration de base de données s'exécutera automatiquement et ajoutera les colonnes nécessaires.

### 2. Frontend

```bash
cd frontend
npm install
ng serve
```

### 3. Test de la fonctionnalité

1. **Connexion**

   - Connectez-vous avec un compte existant
   - Vous arriverez sur la page d'accueil

2. **Accès au profil**

   - Cliquez sur la carte "Gérer le profil" (orange avec icône user-check)
   - Vous serez redirigé vers `/profile`

3. **Affichage du profil**

   - Vérifiez que vos informations s'affichent correctement
   - Notez le statut de vérification de l'email
   - Notez le type de compte (local ou Google)

4. **Modification du profil**

   - Cliquez sur "Modifier le profil"
   - Modifiez votre nom d'utilisateur ou email
   - Pour les comptes locaux, vous pouvez aussi changer le mot de passe
   - Cliquez sur "Enregistrer"

5. **Test de suppression de compte**
   - Cliquez sur "Supprimer le compte"
   - Entrez votre email pour confirmer
   - Cliquez sur "Envoyer l'email de confirmation"
   - Consultez votre boîte email
   - Cliquez sur le lien de confirmation
   - Votre compte sera supprimé et vous serez déconnecté

## Endpoints API

### Récupérer le profil

```http
GET http://localhost:8080/api/profile
Authorization: Bearer <token>
```

### Mettre à jour le profil

```http
PUT http://localhost:8080/api/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "nouveau_nom",
  "email": "nouveau@email.com",
  "currentPassword": "ancien_mdp",
  "newPassword": "nouveau_mdp"
}
```

### Demander la suppression

```http
POST http://localhost:8080/api/profile/request-delete
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "votre@email.com"
}
```

### Confirmer la suppression

```http
DELETE http://localhost:8080/api/profile/confirm-delete?token=<delete_token>
Authorization: Bearer <token>
```

## Vérifications

### Base de données

Vérifiez que les nouvelles colonnes ont été ajoutées :

```sql
SELECT delete_token, delete_token_expiry FROM users;
```

### Emails

Assurez-vous que votre configuration Brevo est correcte dans `application.properties` :

```properties
brevo.api.key=votre_clé_api
brevo.sender.email=votre@email.com
brevo.sender.name=9awi Niveau
app.frontend.url=http://localhost:4200
```

## Dépannage

### Le token n'est pas envoyé

- Vérifiez que vous êtes connecté
- Vérifiez que le token est dans localStorage : `localStorage.getItem('auth-token')`

### Erreur 401 Unauthorized

- Le token a peut-être expiré, reconnectez-vous
- Vérifiez que le token est bien envoyé dans l'en-tête Authorization

### L'email n'est pas reçu

- Vérifiez votre configuration Brevo
- Vérifiez les logs du backend pour les erreurs d'envoi
- Vérifiez votre dossier spam

### Erreur de migration de base de données

- Si la migration échoue, supprimez manuellement les colonnes et relancez :

```sql
ALTER TABLE users DROP COLUMN IF EXISTS delete_token;
ALTER TABLE users DROP COLUMN IF EXISTS delete_token_expiry;
```
