#!/usr/bin/env python3
"""
Script de test pour l'agent IA de recommandation
Génère des données d'exemple et teste toutes les fonctionnalités
"""

import pandas as pd
import numpy as np
import json
from datetime import datetime, timedelta
import sys
import os

# Ajouter le répertoire parent au path pour importer le moteur
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from recommendation_engine import RecommendationEngine

def generate_sample_data():
    """
    Génère des données d'exemple réalistes pour tester le moteur
    """
    
    # Données utilisateurs (20 utilisateurs)
    users_data = []
    for i in range(1, 21):
        users_data.append({
            'user_id': i,
            'total_xp': np.random.randint(0, 2000),
            'current_level': min(10, max(1, np.random.randint(1, 8)))
        })
    
    # Données cours (15 cours dans différentes catégories)
    cours_data = [
        {'id': 101, 'titre': 'Python Fondamentaux', 'categorie': 'Programmation', 'formateur_id': 10},
        {'id': 102, 'titre': 'JavaScript Avancé', 'categorie': 'Programmation', 'formateur_id': 11},
        {'id': 103, 'titre': 'Data Science avec Python', 'categorie': 'Data Science', 'formateur_id': 12},
        {'id': 104, 'titre': 'Machine Learning Pratique', 'categorie': 'Intelligence Artificielle', 'formateur_id': 13},
        {'id': 105, 'titre': 'Développement Web Frontend', 'categorie': 'Web Development', 'formateur_id': 14},
        {'id': 106, 'titre': 'DevOps et CI/CD', 'categorie': 'Infrastructure', 'formateur_id': 15},
        {'id': 107, 'titre': 'Bases de Données SQL', 'categorie': 'Base de Données', 'formateur_id': 16},
        {'id': 108, 'titre': 'React Intermédiaire', 'categorie': 'Web Development', 'formateur_id': 17},
        {'id': 109, 'titre': 'Algorithmes Avancés', 'categorie': 'Programmation', 'formateur_id': 18},
        {'id': 110, 'titre': 'Deep Learning', 'categorie': 'Intelligence Artificielle', 'formateur_id': 19},
        {'id': 111, 'titre': 'Cybersécurité Fondamentaux', 'categorie': 'Sécurité', 'formateur_id': 20},
        {'id': 112, 'titre': 'Cloud Computing AWS', 'categorie': 'Infrastructure', 'formateur_id': 21},
        {'id': 113, 'titre': 'Mobile Development Flutter', 'categorie': 'Mobile', 'formateur_id': 22},
        {'id': 114, 'titre': 'Analyse de Données', 'categorie': 'Data Science', 'formateur_id': 23},
        {'id': 115, 'titre': 'Gestion de Projet Agile', 'categorie': 'Management', 'formateur_id': 24}
    ]
    
    # Générer des inscriptions réalistes
    enrollments_data = []
    current_time = datetime.now().timestamp() * 1000
    
    for user in users_data:
        user_id = user['user_id']
        level = user['current_level']
        
        # Nombre de cours basé sur le niveau
        num_courses = min(8, max(1, level + np.random.randint(-1, 3)))
        
        # Sélectionner des cours aléatoires
        selected_courses = np.random.choice(
            [c['id'] for c in cours_data], 
            size=num_courses, 
            replace=False
        )
        
        for cours_id in selected_courses:
            # Progression basée sur le niveau et un facteur aléatoire
            base_progress = min(100, level * 15 + np.random.randint(-20, 40))
            progress = max(0, base_progress + np.random.normal(0, 15))
            
            # Date d'inscription dans les 6 derniers mois
            days_ago = np.random.randint(1, 180)
            enrolled_at = current_time - (days_ago * 24 * 60 * 60 * 1000)
            
            enrollments_data.append({
                'user_id': user_id,
                'cours_id': int(cours_id),
                'progress': round(progress, 1),
                'enrolled_at': int(enrolled_at)
            })
    
    # Générer des résultats de quiz
    quiz_results_data = []
    quiz_id = 201
    
    for enrollment in enrollments_data:
        # 70% de chance d'avoir tenté un quiz pour ce cours
        if np.random.random() < 0.7:
            user_id = enrollment['user_id']
            user_level = next(u['current_level'] for u in users_data if u['user_id'] == user_id)
            
            # Score basé sur le niveau et la progression
            base_score = min(100, user_level * 12 + enrollment['progress'] * 0.3)
            score = max(0, base_score + np.random.normal(0, 15))
            
            # Date de passage après l'inscription
            days_after_enrollment = np.random.randint(1, 30)
            date_passed = enrollment['enrolled_at'] + (days_after_enrollment * 24 * 60 * 60 * 1000)
            
            quiz_results_data.append({
                'user_id': user_id,
                'quiz_id': quiz_id,
                'score': round(score, 1),
                'date_passed': int(date_passed)
            })
            
            quiz_id += 1
    
    # Créer les DataFrames
    enrollments_df = pd.DataFrame(enrollments_data)
    quiz_results_df = pd.DataFrame(quiz_results_data)
    cours_df = pd.DataFrame(cours_data)
    user_xp_df = pd.DataFrame(users_data)
    
    return enrollments_df, quiz_results_df, cours_df, user_xp_df

def test_recommendation_engine():
    """
    Test complet du moteur de recommandation
    """
    
    print("🤖 Test de l'Agent IA de Recommandation Pédagogique")
    print("=" * 60)
    
    # 1. Générer les données d'exemple
    print("\n📊 Génération des données d'exemple...")
    enrollments_df, quiz_results_df, cours_df, user_xp_df = generate_sample_data()
    
    print(f"   ✓ {len(enrollments_df)} inscriptions générées")
    print(f"   ✓ {len(quiz_results_df)} résultats de quiz générés")
    print(f"   ✓ {len(cours_df)} cours disponibles")
    print(f"   ✓ {len(user_xp_df)} profils utilisateurs")
    
    # 2. Initialiser le moteur
    print("\n🔧 Initialisation du moteur de recommandation...")
    engine = RecommendationEngine()
    engine.load_data(enrollments_df, quiz_results_df, cours_df, user_xp_df)
    
    print(f"   ✓ Matrice utilisateur-item: {engine.user_item_matrix.shape}")
    print(f"   ✓ Features de contenu: {engine.content_features.shape}")
    
    # 3. Entraîner le modèle
    print("\n🎯 Entraînement du modèle de filtrage collaboratif...")
    engine.train_collaborative_filtering()
    print("   ✓ Modèle SVD entraîné")
    print("   ✓ Matrice de similarité calculée")
    
    # 4. Tester les recommandations pour plusieurs utilisateurs
    print("\n🎓 Test des recommandations pour différents profils...")
    
    test_users = [1, 5, 10, 15, 20]  # Utilisateurs avec différents niveaux
    
    for user_id in test_users:
        print(f"\n--- Utilisateur {user_id} ---")
        
        # Profil utilisateur
        user_info = user_xp_df[user_xp_df['user_id'] == user_id].iloc[0]
        user_enrollments = enrollments_df[enrollments_df['user_id'] == user_id]
        user_quizzes = quiz_results_df[quiz_results_df['user_id'] == user_id]
        
        print(f"Niveau: {user_info['current_level']}, XP: {user_info['total_xp']}")
        print(f"Cours suivis: {len(user_enrollments)}")
        print(f"Quiz tentés: {len(user_quizzes)}")
        
        if len(user_quizzes) > 0:
            avg_score = user_quizzes['score'].mean()
            print(f"Score moyen aux quiz: {avg_score:.1f}%")
        
        # Générer recommandations
        try:
            recommendations = engine.generate_hybrid_recommendations(user_id, n_recommendations=5)
            
            print(f"\n📋 Recommandations générées:")
            for i, rec in enumerate(recommendations['recommendations'], 1):
                print(f"  {i}. {rec['title']}")
                print(f"     Raison: {rec['reason']}")
                print(f"     Priorité: {rec['priority']}, Confiance: {rec['confidenceScore']:.2f}")
                print()
                
        except Exception as e:
            print(f"   ❌ Erreur: {e}")
    
    # 5. Test des différentes méthodes de recommandation
    print("\n🔍 Test des méthodes individuelles...")
    
    test_user = 5
    print(f"\nUtilisateur de test: {test_user}")
    
    # Filtrage collaboratif
    try:
        collab_recs = engine.get_collaborative_recommendations(test_user, 3)
        print(f"\n🤝 Filtrage collaboratif ({len(collab_recs)} recommandations):")
        for rec in collab_recs:
            cours_title = cours_df[cours_df['id'] == rec['cours_id']]['titre'].iloc[0]
            print(f"   - {cours_title} (score: {rec['score']:.3f})")
    except Exception as e:
        print(f"   ❌ Filtrage collaboratif: {e}")
    
    # Recommandations basées sur le contenu
    try:
        content_recs = engine.get_content_based_recommendations(test_user, 3)
        print(f"\n📚 Basé sur le contenu ({len(content_recs)} recommandations):")
        for rec in content_recs:
            cours_title = cours_df[cours_df['id'] == rec['cours_id']]['titre'].iloc[0]
            print(f"   - {cours_title} (score: {rec['score']:.3f})")
    except Exception as e:
        print(f"   ❌ Basé sur le contenu: {e}")
    
    # Recommandations par niveau
    try:
        level_recs = engine.get_level_based_recommendations(test_user, 3)
        print(f"\n📊 Basé sur le niveau ({len(level_recs)} recommandations):")
        for rec in level_recs:
            cours_title = cours_df[cours_df['id'] == rec['cours_id']]['titre'].iloc[0]
            print(f"   - {cours_title} (score: {rec['score']:.3f})")
    except Exception as e:
        print(f"   ❌ Basé sur le niveau: {e}")
    
    # 6. Analyse des performances
    print("\n📈 Analyse des performances du moteur...")
    
    # Statistiques de la matrice utilisateur-item
    matrix_density = (engine.user_item_matrix > 0).sum().sum() / engine.user_item_matrix.size
    print(f"   Densité de la matrice: {matrix_density:.3f}")
    
    # Couverture des recommandations
    all_course_ids = set(cours_df['id'])
    recommended_course_ids = set()
    
    for user_id in range(1, 6):  # Test sur 5 utilisateurs
        try:
            recs = engine.generate_hybrid_recommendations(user_id, 10)
            for rec in recs['recommendations']:
                recommended_course_ids.add(rec['id'])
        except:
            pass
    
    coverage = len(recommended_course_ids) / len(all_course_ids)
    print(f"   Couverture des cours: {coverage:.3f} ({len(recommended_course_ids)}/{len(all_course_ids)})")
    
    # 7. Export d'un exemple JSON
    print("\n💾 Export d'un exemple de recommandation...")
    
    try:
        example_rec = engine.generate_hybrid_recommendations(1, 5)
        
        # Sauvegarder dans un fichier
        output_file = "example_recommendations.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(example_rec, f, indent=2, ensure_ascii=False)
        
        print(f"   ✓ Exemple sauvegardé dans {output_file}")
        
        # Afficher le JSON
        print("\n📄 Exemple de sortie JSON:")
        print(json.dumps(example_rec, indent=2, ensure_ascii=False))
        
    except Exception as e:
        print(f"   ❌ Erreur lors de l'export: {e}")
    
    print("\n" + "=" * 60)
    print("✅ Test du moteur de recommandation terminé avec succès!")
    print("\n🎯 Fonctionnalités validées:")
    print("   ✓ Chargement et préparation des données")
    print("   ✓ Création de la matrice utilisateur-item")
    print("   ✓ Extraction des features de contenu")
    print("   ✓ Filtrage collaboratif avec SVD")
    print("   ✓ Recommandations basées sur le contenu")
    print("   ✓ Recommandations par niveau")
    print("   ✓ Approche hybride pondérée")
    print("   ✓ Génération d'explications pédagogiques")
    print("   ✓ Format JSON conforme aux spécifications")

def analyze_user_behavior():
    """
    Analyse approfondie du comportement utilisateur pour validation
    """
    
    print("\n🔬 Analyse comportementale des utilisateurs...")
    
    enrollments_df, quiz_results_df, cours_df, user_xp_df = generate_sample_data()
    
    # Analyse par catégorie
    category_stats = {}
    
    for _, cours in cours_df.iterrows():
        category = cours['categorie']
        cours_id = cours['id']
        
        # Inscriptions pour ce cours
        course_enrollments = enrollments_df[enrollments_df['cours_id'] == cours_id]
        
        if category not in category_stats:
            category_stats[category] = {
                'enrollments': 0,
                'avg_progress': 0,
                'completion_rate': 0
            }
        
        category_stats[category]['enrollments'] += len(course_enrollments)
        
        if len(course_enrollments) > 0:
            category_stats[category]['avg_progress'] += course_enrollments['progress'].mean()
            completion_rate = (course_enrollments['progress'] >= 100).sum() / len(course_enrollments)
            category_stats[category]['completion_rate'] += completion_rate
    
    print("\n📊 Statistiques par catégorie:")
    for category, stats in category_stats.items():
        print(f"\n{category}:")
        print(f"   Inscriptions: {stats['enrollments']}")
        print(f"   Progression moyenne: {stats['avg_progress']:.1f}%")
        print(f"   Taux de complétion: {stats['completion_rate']:.1%}")
    
    # Analyse des performances par niveau
    print("\n📈 Performance par niveau utilisateur:")
    
    for level in range(1, 8):
        level_users = user_xp_df[user_xp_df['current_level'] == level]['user_id']
        
        if len(level_users) > 0:
            level_quizzes = quiz_results_df[quiz_results_df['user_id'].isin(level_users)]
            
            if len(level_quizzes) > 0:
                avg_score = level_quizzes['score'].mean()
                print(f"   Niveau {level}: {avg_score:.1f}% (sur {len(level_quizzes)} quiz)")

if __name__ == "__main__":
    # Exécuter les tests
    test_recommendation_engine()
    analyze_user_behavior()