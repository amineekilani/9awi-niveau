# 🧪 Guide de Test - Système de Recommandations IA

## 🚀 Démarrage Rapide

### 1. Configuration de la Base de Données

```sql
-- Exécuter dans votre base de données MySQL
source backend/quick_start_recommendations.sql;
```

### 2. Démarrage des Services

**Backend (Spring Boot)**

```bash
cd backend
mvn spring-boot:run
```

**Frontend (Angular)**

```bash
cd frontend
ng serve
```

### 3. Accès à l'Application

- **URL** : http://localhost:4200
- **Connexion** : Utilisez un compte étudiant existant

## 🧪 Tests à Effectuer

### Test 1 : Widget Page d'Accueil

1. **Connexion** en tant qu'étudiant
2. **Page d'accueil** : Vérifier la présence du widget "Recommandations IA pour Vous"
3. **Chargement** : Observer le spinner puis l'affichage des recommandations
4. **Cartes** : Vérifier les scores IA, badges de correspondance, raisons
5. **Clic** : Tester le lien vers un parcours recommandé

**✅ Résultat attendu :**

- Widget visible avec 3 recommandations maximum
- Scores affichés (ex: 85%, 72%, 64%)
- Badges colorés (PARFAIT, BON, ACCEPTABLE)
- Raisons explicites ("Correspond à vos centres d'intérêt")

### Test 2 : Page Recommandations Complète

1. **Navigation** : Cliquer sur "Recommandations" dans la navbar
2. **Onglet Personnalisées** : Vérifier l'affichage des recommandations IA
3. **Scores détaillés** : Observer les pourcentages et couleurs
4. **Actions** : Tester "Voir le Parcours" et "S'inscrire"

**✅ Résultat attendu :**

- Page avec 3 onglets fonctionnels
- Recommandations triées par score décroissant
- Cartes détaillées avec métadonnées complètes
- Actions fonctionnelles

### Test 3 : Recherche par Critères

1. **Onglet "Recherche par Critères"**
2. **Filtres** : Sélectionner des catégories, niveau de difficulté
3. **Objectifs** : Cocher plusieurs objectifs d'apprentissage
4. **Recherche** : Cliquer sur "Rechercher"
5. **Résultats** : Vérifier le filtrage et le tri

**✅ Résultat attendu :**

- Filtres multiples fonctionnels
- Résultats adaptés aux critères sélectionnés
- Message si aucun résultat trouvé
- Bouton "Effacer" fonctionnel

### Test 4 : Configuration des Préférences

1. **Onglet "Mes Préférences"**
2. **Formulaire** : Remplir les différents champs
3. **Sélections multiples** : Catégories, objectifs, centres d'intérêt
4. **Sauvegarde** : Cliquer sur "Sauvegarder les Préférences"
5. **Vérification** : Retourner aux recommandations personnalisées

**✅ Résultat attendu :**

- Formulaire complet et intuitif
- Sauvegarde avec message de succès
- Impact immédiat sur les recommandations
- Données persistantes après rechargement

### Test 5 : API Backend

**Test des endpoints avec un outil comme Postman ou curl :**

```bash
# Recommandations personnalisées
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/recommendations/personalized

# Recommandations rapides
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/recommendations/quick

# Préférences utilisateur
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/recommendations/preferences
```

**✅ Résultat attendu :**

- Réponses JSON valides
- Scores calculés correctement
- Données cohérentes avec l'interface

## 🔍 Points de Vérification

### Interface Utilisateur

- [ ] Widget visible sur la page d'accueil (étudiants uniquement)
- [ ] Lien "Recommandations" dans la navbar
- [ ] Page recommandations avec 3 onglets
- [ ] Design responsive et moderne
- [ ] Icônes Feather correctement affichées
- [ ] Messages d'erreur/succès appropriés

### Fonctionnalités IA

- [ ] Scores de recommandation calculés (0-100%)
- [ ] Badges de correspondance (PARFAIT/BON/ACCEPTABLE)
- [ ] Raisons de recommandation générées
- [ ] Tri par pertinence décroissante
- [ ] Filtrage des parcours déjà inscrits
- [ ] Prise en compte des préférences utilisateur

### Performance

- [ ] Chargement rapide des recommandations (< 2s)
- [ ] Pas d'erreurs dans la console navigateur
- [ ] Pas d'erreurs dans les logs backend
- [ ] Interface réactive et fluide

### Données

- [ ] Table `user_preferences` créée
- [ ] Préférences par défaut insérées
- [ ] Cohérence des données entre frontend/backend
- [ ] Sauvegarde persistante des préférences

## 🐛 Résolution de Problèmes

### Problème : Aucune recommandation affichée

**Causes possibles :**

- Aucun parcours publié dans la base
- Utilisateur inscrit à tous les parcours
- Erreur de configuration de la base de données

**Solutions :**

1. Vérifier les parcours publiés : `SELECT * FROM parcours_apprentissage WHERE is_published = true;`
2. Créer des parcours de test via l'interface formateur
3. Vérifier les logs backend pour les erreurs

### Problème : Erreur 500 sur les API

**Causes possibles :**

- Table `user_preferences` manquante
- Méthodes repository manquantes
- Erreur de configuration Spring Boot

**Solutions :**

1. Exécuter `quick_start_recommendations.sql`
2. Vérifier les logs Spring Boot
3. Redémarrer l'application backend

### Problème : Scores toujours à 0%

**Causes possibles :**

- Préférences utilisateur vides
- Algorithme de scoring défaillant
- Données de test insuffisantes

**Solutions :**

1. Configurer les préférences via l'interface
2. Vérifier les données de test avec `test_recommendations_system.sql`
3. Consulter les logs pour les erreurs de calcul

### Problème : Interface non responsive

**Causes possibles :**

- CSS Tailwind non chargé
- Erreurs JavaScript
- Composants Angular non initialisés

**Solutions :**

1. Vérifier la console navigateur
2. Redémarrer le serveur Angular
3. Vider le cache navigateur

## 📊 Métriques de Succès

### Fonctionnalité

- ✅ **100%** des endpoints API fonctionnels
- ✅ **3 onglets** dans l'interface recommandations
- ✅ **6 critères** pris en compte dans l'algorithme IA
- ✅ **Widget** intégré sur la page d'accueil

### Performance

- ✅ **< 2 secondes** pour charger les recommandations
- ✅ **0 erreur** dans les logs en fonctionnement normal
- ✅ **Responsive** sur mobile, tablette, desktop

### Expérience Utilisateur

- ✅ **Interface intuitive** sans formation nécessaire
- ✅ **Feedback visuel** avec scores et raisons
- ✅ **Personnalisation** via les préférences
- ✅ **Actions directes** (voir, s'inscrire)

## 🎯 Prochaines Étapes

Après validation des tests :

1. **Déploiement en production**
2. **Collecte des métriques d'usage**
3. **Optimisation des algorithmes** basée sur les données réelles
4. **Ajout de fonctionnalités avancées** (ML, recommandations sociales)

---

**Le système de recommandations IA est maintenant prêt à transformer l'expérience d'apprentissage de vos utilisateurs !** 🚀✨
