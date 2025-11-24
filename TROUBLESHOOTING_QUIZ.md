# Résolution de problèmes - Quiz

## Problème : quiz.id est undefined

### Cause

Le quiz n'est pas chargé correctement après sa création.

### Solution appliquée

1. Le backend retourne maintenant `204 No Content` au lieu d'un message quand il n'y a pas de quiz
2. Le frontend vérifie que `data.id` existe avant d'assigner le quiz
3. Après création du quiz, on assigne immédiatement la réponse à `this.quiz`
4. Vérification avant d'ajouter une question que le quiz existe et a un ID

### Comment vérifier

#### Console du navigateur

Après avoir créé un quiz, vous devriez voir :

```
Quiz créé: {id: 1, titre: "...", ...}
Quiz chargé: {id: 1, titre: "...", ...}
Quiz ID: 1
```

Si vous voyez `Quiz ID: undefined`, c'est qu'il y a un problème.

#### Console du serveur

Vérifiez que le quiz est bien créé :

```
Quiz créé avec ID: 1
```

## Problème : 401 Unauthorized

### Cause

Le token JWT n'est pas envoyé ou est expiré.

### Solutions

#### 1. Vérifier que vous êtes connecté

- Déconnectez-vous et reconnectez-vous
- Vérifiez que le token est dans le localStorage :

```javascript
console.log(localStorage.getItem("token"));
```

#### 2. Vérifier l'intercepteur JWT

Le fichier `jwt.interceptor.ts` doit ajouter le token à chaque requête.

#### 3. Vérifier les endpoints

Tous les endpoints quiz nécessitent une authentification sauf `GET /api/quiz/module/{moduleId}` et `GET /api/quiz/{quizId}`.

## Étapes de débogage

### 1. Créer un quiz

```
1. Accédez à un module
2. Cliquez sur "Créer un quiz"
3. Remplissez le formulaire
4. Cliquez sur "Créer le quiz"
5. Vérifiez la console : "Quiz créé: {id: X, ...}"
```

### 2. Ajouter une question

```
1. Cliquez sur "Ajouter une question"
2. Remplissez le formulaire
3. Vérifiez la console : "Ajout de la question au quiz: X"
4. Cliquez sur "Ajouter la question"
5. Vérifiez la console : "Question ajoutée: {id: Y, ...}"
```

### 3. Vérifier la base de données

```sql
-- Vérifier le quiz
SELECT * FROM quiz WHERE module_id = X;

-- Vérifier les questions
SELECT id, question, options, correct_answer FROM question WHERE quiz_id = Y;
```

## Messages d'erreur courants

### "Le quiz n'est pas chargé correctement"

- Rechargez la page (Ctrl+F5)
- Vérifiez que le quiz existe dans la base de données
- Vérifiez les logs de la console

### "Toutes les options doivent être remplies"

- Assurez-vous que chaque champ d'option contient du texte
- Supprimez les options vides avec le bouton X

### "La réponse correcte doit être l'une des options"

- Sélectionnez la réponse correcte dans la liste déroulante
- Ne tapez pas manuellement la réponse

### "Vous n'êtes pas autorisé..."

- Vérifiez que vous êtes connecté en tant que formateur
- Vérifiez que vous êtes le propriétaire du cours
- Reconnectez-vous

## Checklist de vérification

- [ ] Base de données : Tables `quiz` et `question` créées
- [ ] Backend : Serveur Spring Boot démarré
- [ ] Frontend : Serveur Angular démarré
- [ ] Authentification : Connecté en tant que formateur
- [ ] Module : Vous êtes sur la page d'un module
- [ ] Quiz : Le quiz est créé et visible
- [ ] Console : Pas d'erreurs dans la console du navigateur
- [ ] Console : Pas d'erreurs dans la console du serveur

## Logs à vérifier

### Frontend (Console navigateur)

```
Quiz créé: {...}
Quiz chargé: {...}
Quiz ID: 1
=== Sauvegarde de question ===
Question form: {...}
Options: [...]
Ajout de la question au quiz: 1
Question ajoutée: {...}
```

### Backend (Console serveur)

```
=== Ajout de question ===
Quiz ID: 1
Question: ...
Options: [...]
Réponse correcte: ...
Question avant sauvegarde - Options: [...]
Question sauvegardée - ID: 1
Question sauvegardée - Options: [...]
```

## Redémarrage complet

Si rien ne fonctionne :

1. Arrêtez le backend et le frontend
2. Vérifiez la base de données :

```sql
SELECT * FROM quiz;
SELECT * FROM question;
```

3. Redémarrez le backend
4. Redémarrez le frontend
5. Videz le cache du navigateur (Ctrl+Shift+Delete)
6. Reconnectez-vous
7. Testez à nouveau
