# Guide d'utilisation - Système de Quiz

## 🎯 Fonctionnalités

Le système de quiz permet aux formateurs de créer des évaluations pour chaque module de cours.

### Caractéristiques principales

- **Un quiz par module** - Chaque module peut avoir un seul quiz
- **Questions à choix multiples** - Avec plusieurs options de réponse
- **Gestion complète** - Créer, modifier, supprimer quiz et questions
- **Ordre personnalisable** - Définir l'ordre d'affichage des questions
- **Validation automatique** - La réponse correcte est marquée visuellement

## 📋 Structure

### Entités Backend

1. **Quiz**

   - Titre
   - Description
   - Lié à un module (relation OneToOne)
   - Liste de questions

2. **Question**
   - Texte de la question
   - Liste d'options (minimum 2)
   - Réponse correcte
   - Ordre d'affichage

## 🚀 Installation

### 1. Base de données

Exécutez le script de migration :

```bash
mysql -u root -p votre_database < backend/migration_add_quiz.sql
```

### 2. Backend

Les fichiers suivants ont été créés :

- `entity/Quiz.java`
- `entity/Question.java`
- `dto/QuizRequest.java`
- `dto/QuizResponse.java`
- `dto/QuestionRequest.java`
- `dto/QuestionResponse.java`
- `repository/QuizRepository.java`
- `repository/QuestionRepository.java`
- `service/QuizService.java`
- `controller/QuizController.java`

Redémarrez le serveur Spring Boot.

### 3. Frontend

Le fichier suivant a été créé :

- `quiz.service.ts`

Le composant `module-detail` a été mis à jour pour inclure la gestion des quiz.

Redémarrez le serveur Angular (`ng serve`).

## 📖 Utilisation

### Pour les formateurs

#### 1. Créer un quiz

1. Accédez à un module de cours
2. Scrollez jusqu'à la section "Quiz du module"
3. Cliquez sur "Créer un quiz"
4. Remplissez le titre et la description
5. Cliquez sur "Créer le quiz"

#### 2. Ajouter des questions

1. Une fois le quiz créé, cliquez sur "Ajouter une question"
2. Saisissez le texte de la question
3. Ajoutez au moins 2 options de réponse
4. Sélectionnez la réponse correcte dans la liste déroulante
5. (Optionnel) Définissez l'ordre d'affichage
6. Cliquez sur "Ajouter la question"

#### 3. Modifier une question

1. Cliquez sur le bouton "Modifier" (icône crayon) à côté de la question
2. Modifiez les informations
3. Cliquez sur "Enregistrer"

#### 4. Supprimer une question

1. Cliquez sur le bouton "Supprimer" (icône corbeille)
2. Confirmez la suppression

#### 5. Modifier le quiz

1. Cliquez sur "Modifier" dans l'en-tête du quiz
2. Modifiez le titre ou la description
3. Cliquez sur "Enregistrer"

#### 6. Supprimer le quiz

1. Cliquez sur "Supprimer" dans l'en-tête du quiz
2. Confirmez la suppression (toutes les questions seront supprimées)

### Pour les étudiants

L'interface d'affichage et de passage du quiz sera développée ultérieurement.

## 🔒 Sécurité

- Seul le formateur propriétaire du cours peut gérer le quiz
- Vérification des permissions à chaque opération
- Validation des données côté backend et frontend

## 🎨 Interface

### Design

- Section dédiée avec icône de quiz
- Couleur thématique violette/pourpre
- Questions numérotées avec badges
- Réponse correcte marquée en vert avec icône de validation
- Animations fluides

### Responsive

- Adapté aux mobiles et tablettes
- Boutons empilés sur petits écrans
- Grille flexible

## 📡 API Endpoints

### Quiz

- `POST /api/quiz/module/{moduleId}` - Créer un quiz
- `PUT /api/quiz/{quizId}` - Modifier un quiz
- `DELETE /api/quiz/{quizId}` - Supprimer un quiz
- `GET /api/quiz/module/{moduleId}` - Récupérer le quiz d'un module
- `GET /api/quiz/{quizId}` - Récupérer un quiz par ID

### Questions

- `POST /api/quiz/{quizId}/question` - Ajouter une question
- `PUT /api/quiz/question/{questionId}` - Modifier une question
- `DELETE /api/quiz/question/{questionId}` - Supprimer une question

## 🔄 Prochaines étapes

1. **Interface étudiant** - Permettre aux étudiants de passer le quiz
2. **Système de notation** - Calculer et enregistrer les scores
3. **Historique des tentatives** - Suivre les passages de quiz
4. **Statistiques** - Analyser les résultats des étudiants
5. **Types de questions** - Ajouter vrai/faux, réponse courte, etc.
6. **Timer** - Ajouter une limite de temps pour le quiz
7. **Randomisation** - Mélanger l'ordre des questions et options

## ⚠️ Notes importantes

- Un module ne peut avoir qu'un seul quiz
- Une question doit avoir au moins 2 options
- La réponse correcte doit être l'une des options proposées
- Les questions sont triées par ordre croissant (si défini)
- La suppression d'un quiz supprime toutes ses questions (cascade)
