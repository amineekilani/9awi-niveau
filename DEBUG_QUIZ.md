# Guide de débogage - Système de Quiz

## Problème résolu

Les options des questions sont maintenant stockées dans un seul champ TEXT au format JSON au lieu d'une table séparée.

## Modifications apportées

### 1. Entité Question

- Ajout de `optionsJson` (String) pour stocker les options en JSON
- `options` est maintenant `@Transient` (non persisté directement)
- Méthodes de sérialisation/désérialisation automatiques avec Jackson
- Hooks JPA : `@PrePersist`, `@PreUpdate`, `@PostLoad`

### 2. Migration SQL

- Suppression de la table `question_options`
- Ajout de la colonne `options` (TEXT) dans la table `question`
- Script de mise à jour : `migration_update_quiz.sql`

### 3. Logs de débogage

- Backend : Logs dans `QuizService.addQuestion()`
- Frontend : Logs dans `saveQuestion()`

## Installation

### Si vous n'avez pas encore créé les tables

```bash
mysql -u root -p votre_database < backend/migration_add_quiz.sql
```

### Si vous avez déjà créé les tables avec l'ancien script

```bash
mysql -u root -p votre_database < backend/migration_update_quiz.sql
```

### Ou recommencer à zéro

```sql
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS quiz;
```

Puis exécutez `migration_add_quiz.sql`

## Comment déboguer

### 1. Console du navigateur (F12)

Lors de l'ajout d'une question, vous verrez :

```
=== Sauvegarde de question ===
Question form: {...}
Options: ["Option 1", "Option 2", "Option 3", "Option 4"]
Réponse correcte: "Option 1"
Ajout de la question au quiz: 1
Question ajoutée: {...}
```

### 2. Console du serveur Spring Boot

Vous verrez :

```
=== Ajout de question ===
Quiz ID: 1
Question: Quelle est la capitale de la France ?
Options: [Paris, Londres, Berlin, Madrid]
Réponse correcte: Paris
Question avant sauvegarde - Options: [Paris, Londres, Berlin, Madrid]
Question sauvegardée - ID: 1
Question sauvegardée - Options: [Paris, Londres, Berlin, Madrid]
```

### 3. Vérifier la base de données

```sql
SELECT id, question, options, correct_answer FROM question;
```

Vous devriez voir :

```
id | question                              | options                                    | correct_answer
1  | Quelle est la capitale de la France ? | ["Paris","Londres","Berlin","Madrid"]     | Paris
```

## Structure JSON des options

Les options sont stockées au format JSON dans la base de données :

```json
["Option 1", "Option 2", "Option 3", "Option 4"]
```

## Problèmes courants

### Les options ne s'affichent pas

1. Vérifiez que la colonne `options` existe dans la table `question`
2. Vérifiez que les données sont bien au format JSON
3. Vérifiez les logs backend pour voir si la désérialisation fonctionne

### Erreur lors de l'ajout

1. Vérifiez que toutes les options sont remplies
2. Vérifiez que la réponse correcte est bien l'une des options
3. Vérifiez les logs pour voir où l'erreur se produit

### Les options sont vides après rechargement

1. Vérifiez que `@PostLoad` est bien appelé
2. Vérifiez que le JSON est valide dans la base de données
3. Ajoutez des logs dans `deserializeOptions()`

## Test manuel

1. Créez un quiz pour un module
2. Ajoutez une question avec 4 options
3. Sélectionnez la bonne réponse
4. Cliquez sur "Ajouter la question"
5. Vérifiez dans la console que les logs s'affichent
6. Rechargez la page
7. Les options doivent s'afficher correctement

## Redémarrage

Après les modifications :

1. Redémarrez le serveur Spring Boot
2. Rechargez la page Angular (Ctrl+F5)
3. Testez l'ajout d'une question
