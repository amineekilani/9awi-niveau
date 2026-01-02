# Guide de Démarrage Rapide - Gamification

## 🚀 Mise en Route en 5 Minutes

### Étape 1 : Initialisation du Système

**Option A : Via Script (Recommandé)**

```bash
cd backend
./fix_gamification_issues.bat
```

**Option B : Via API**

```bash
# Démarrer le backend puis :
curl -X POST http://localhost:8080/api/init/gamification-tables
curl -X POST http://localhost:8080/api/init/fix-duplicates
```

### Étape 2 : Vérification

1. Connectez-vous en tant qu'administrateur
2. Accédez à la section "Gamification" dans l'interface admin
3. Vérifiez que les badges et niveaux par défaut sont présents

### Étape 3 : Test

1. Créez un quiz et soumettez-le en tant qu'utilisateur
2. Vérifiez que les XP sont attribués
3. Consultez le classement dans l'interface admin

## 🔧 Résolution des Problèmes Courants

### Erreur "Query did not return a unique result"

```bash
curl -X POST http://localhost:8080/api/init/fix-duplicates
```

### Tables de gamification manquantes

```bash
curl -X POST http://localhost:8080/api/init/gamification-tables
```

### Quiz ne fonctionne plus

- Les erreurs de gamification sont maintenant capturées automatiquement
- Les quiz continuent de fonctionner même si la gamification a des problèmes
- Vérifiez les logs pour identifier les erreurs spécifiques

## 📊 Fonctionnalités Disponibles

### ✅ Implémenté

- Attribution automatique d'XP (quiz, cours)
- Système de badges avec critères
- Niveaux automatiques
- Classements
- Interface d'administration complète
- Protection contre les erreurs

### 🚧 En Développement

- Interface utilisateur pour les apprenants
- Défis temporaires
- Notifications de récompenses

## 🛠️ Maintenance

### Vérification Périodique

```sql
-- Vérifier les doublons
SELECT user_id, COUNT(*) FROM user_xp GROUP BY user_id HAVING COUNT(*) > 1;

-- Vérifier les tables
SHOW TABLES LIKE '%badge%';
SHOW TABLES LIKE '%challenge%';
SHOW TABLES LIKE '%user_xp%';
```

### Scripts Utiles

- `fix_gamification_issues.bat` - Correction complète
- `create_gamification_tables_simple.sql` - Création manuelle
- `fix_user_xp_duplicates.sql` - Suppression doublons

## 📞 Support

En cas de problème :

1. Vérifiez les logs du backend
2. Exécutez les scripts de correction
3. Consultez la documentation complète dans `gamification-admin.md`
