# Guide de Démarrage Rapide - Système de Rôles et Cours

## Étape 1 : Migration de la Base de Données

Exécutez le script SQL pour mettre à jour votre base de données :

```bash
mysql -u root -p 9awi_niveau < backend/migration_add_role.sql
```

Ou connectez-vous à MySQL et exécutez manuellement :

```sql
USE 9awi_niveau;

-- Modifier la colonne role
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ETUDIANT';

-- Mettre à jour les valeurs existantes
UPDATE users SET role = 'ETUDIANT' WHERE role = 'USER' OR role IS NULL;

-- Créer la table cours
CREATE TABLE IF NOT EXISTS cours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    created_at BIGINT,
    updated_at BIGINT,
    archived BOOLEAN DEFAULT FALSE,
    archived_at BIGINT,
    formateur_id BIGINT NOT NULL,
    FOREIGN KEY (formateur_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Créer des index
CREATE INDEX idx_cours_formateur ON cours(formateur_id);
CREATE INDEX idx_cours_archived ON cours(archived);
```

## Étape 2 : Démarrer le Backend

```bash
cd backend
mvnw spring-boot:run
```

Le backend sera accessible sur `http://localhost:8080`

## Étape 3 : Démarrer le Frontend

```bash
cd frontend
npm install  # Si ce n'est pas déjà fait
npm start
```

Le frontend sera accessible sur `http://localhost:4200`

## Étape 4 : Tester le Système

### Test 1 : Compte Étudiant (par défaut)

1. Créer un nouveau compte sur `http://localhost:4200/register`

   - Email : etudiant@test.com
   - Mot de passe : password123
   - Prénom : Jean
   - Nom : Dupont
   - Date de naissance : 2000-01-01

2. Vérifier l'email (consulter les logs du backend pour le lien)

3. Se connecter
   - Vous serez redirigé vers `/cours` (liste des cours)
   - Vous verrez tous les cours disponibles

### Test 2 : Devenir Formateur

1. Aller dans **Profil** (bouton en haut à droite)

2. Dans la section "Changer de rôle" :

   - Sélectionner "Formateur"
   - Cliquer sur "Changer de rôle"

3. Vous serez redirigé vers `/formateur-dashboard`

### Test 3 : Créer un Cours (en tant que Formateur)

1. Sur le dashboard formateur, cliquer sur **"+ Ajouter un cours"**

2. Remplir le formulaire :

   - Titre : "Introduction à Angular"
   - Description : "Apprenez les bases d'Angular avec ce cours complet"

3. Cliquer sur **"Créer"**

4. Le cours apparaît dans votre dashboard

### Test 4 : Modifier un Cours

1. Sur le dashboard formateur, cliquer sur **"Modifier"** pour un cours

2. Modifier le titre ou la description

3. Cliquer sur **"Modifier"**

### Test 5 : Archiver un Cours

1. Sur le dashboard formateur, cliquer sur **"Archiver"** pour un cours

2. Confirmer l'archivage

3. Le cours disparaît de la liste publique mais reste dans votre dashboard

### Test 6 : Vue Étudiant

1. Aller dans **Profil**

2. Changer le rôle vers **"Étudiant"**

3. Vous serez redirigé vers `/cours`

4. Vous verrez tous les cours non archivés avec le nom du formateur

## Étape 5 : Créer un Deuxième Compte pour Tester

1. Déconnectez-vous

2. Créez un nouveau compte : formateur@test.com

3. Changez le rôle vers FORMATEUR

4. Créez quelques cours

5. Reconnectez-vous avec le premier compte (étudiant@test.com)

6. Vérifiez que vous voyez les cours des deux formateurs

## Routes Disponibles

### Routes Publiques

- `/login` - Connexion
- `/register` - Inscription
- `/verify-email` - Vérification d'email
- `/forgot-password` - Mot de passe oublié
- `/reset-password` - Réinitialisation du mot de passe

### Routes Protégées (Authentification requise)

- `/home` - Page d'accueil (redirige selon le rôle)
- `/profile` - Profil utilisateur
- `/cours` - Liste des cours (tous les utilisateurs)
- `/formateur-dashboard` - Dashboard formateur (FORMATEUR uniquement)
- `/cours/nouveau` - Créer un cours (FORMATEUR uniquement)
- `/cours/modifier/:id` - Modifier un cours (FORMATEUR propriétaire uniquement)

## API Endpoints

### Cours

```
POST   /api/cours                 - Créer un cours
PUT    /api/cours/{id}            - Modifier un cours
PUT    /api/cours/{id}/archive    - Archiver un cours
GET    /api/cours/mes-cours       - Mes cours (formateur)
GET    /api/cours                 - Tous les cours
GET    /api/cours/{id}            - Détails d'un cours
```

### Profil

```
GET    /api/profile               - Obtenir le profil
PUT    /api/profile               - Mettre à jour le profil
PUT    /api/profile/change-role   - Changer de rôle
```

## Dépannage

### Problème : "Seuls les formateurs peuvent créer des cours"

- Vérifiez que vous avez bien changé votre rôle vers FORMATEUR dans le profil
- Déconnectez-vous et reconnectez-vous pour rafraîchir le token JWT

### Problème : "Vous n'êtes pas autorisé à modifier ce cours"

- Seul le formateur qui a créé le cours peut le modifier
- Vérifiez que vous êtes connecté avec le bon compte

### Problème : La table cours n'existe pas

- Assurez-vous d'avoir exécuté le script de migration SQL
- Vérifiez que vous êtes connecté à la bonne base de données

### Problème : Le rôle ne change pas

- Vérifiez les logs du backend pour voir les erreurs
- Assurez-vous que la colonne role a été modifiée correctement dans la base de données

## Prochaines Étapes

Une fois le système fonctionnel, vous pouvez :

1. Ajouter plus de champs aux cours (durée, niveau, catégorie)
2. Implémenter un système d'inscription aux cours
3. Ajouter du contenu aux cours (vidéos, documents)
4. Créer un système de notation et commentaires
5. Ajouter des statistiques pour les formateurs
6. Implémenter la recherche et les filtres de cours

## Support

Pour toute question ou problème, consultez :

- `ROLES_ET_COURS_DOCUMENTATION.md` - Documentation complète
- Les logs du backend : `backend/logs/`
- La console du navigateur pour les erreurs frontend
