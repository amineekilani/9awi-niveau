#!/usr/bin/env python3
"""
Agent IA de Recommandation Pédagogique - Kawi Niveau
Système hybride combinant filtrage collaboratif et content-based
"""

import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import TruncatedSVD
from datetime import datetime, timedelta
import json
import logging
from typing import Dict, List, Tuple, Optional
import warnings
warnings.filterwarnings('ignore')

# Configuration du logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class RecommendationEngine:
    """
    Agent IA de recommandation pédagogique pour plateforme e-learning
    Implémente filtrage collaboratif, content-based et approche hybride
    """
    
    def __init__(self):
        self.user_item_matrix = None
        self.content_features = None
        self.similarity_matrix = None
        self.svd_model = None
        self.scaler = StandardScaler()
        
    def load_data(self, enrollments_df: pd.DataFrame, 
                  quiz_results_df: pd.DataFrame,
                  cours_df: pd.DataFrame,
                  user_xp_df: pd.DataFrame) -> None:
        """
        Charge et prépare les données de la plateforme
        
        Args:
            enrollments_df: DataFrame des inscriptions (user_id, cours_id, progress, enrolled_at)
            quiz_results_df: DataFrame des résultats quiz (user_id, quiz_id, score, date_passed)
            cours_df: DataFrame des cours (id, titre, categorie, formateur_id)
            user_xp_df: DataFrame XP utilisateurs (user_id, total_xp, current_level)
        """
        self.enrollments = enrollments_df
        self.quiz_results = quiz_results_df
        self.cours = cours_df
        self.user_xp = user_xp_df
        
        # Créer la matrice utilisateur-item
        self._create_user_item_matrix()
        
        # Extraire les features de contenu
        self._extract_content_features()
        
        logger.info("Données chargées et préparées avec succès")
    
    def _create_user_item_matrix(self) -> None:
        """Crée la matrice utilisateur-cours avec scores pondérés"""
        
        # Calculer le score d'engagement pour chaque inscription
        engagement_scores = []
        
        for _, enrollment in self.enrollments.iterrows():
            user_id = enrollment['user_id']
            cours_id = enrollment['cours_id']
            progress = enrollment['progress']
            
            # Score de base basé sur la progression
            base_score = progress / 100.0
            
            # Bonus pour quiz réussis dans ce cours
            quiz_bonus = self._calculate_quiz_bonus(user_id, cours_id)
            
            # Bonus temporel (activité récente)
            time_bonus = self._calculate_time_bonus(enrollment['enrolled_at'])
            
            # Score final pondéré
            final_score = min(5.0, base_score * 3 + quiz_bonus + time_bonus)
            
            engagement_scores.append({
                'user_id': user_id,
                'cours_id': cours_id,
                'score': final_score
            })
        
        # Créer la matrice pivot
        scores_df = pd.DataFrame(engagement_scores)
        self.user_item_matrix = scores_df.pivot_table(
            index='user_id', 
            columns='cours_id', 
            values='score', 
            fill_value=0
        )
        
        logger.info(f"Matrice utilisateur-item créée: {self.user_item_matrix.shape}")
    
    def _calculate_quiz_bonus(self, user_id: int, cours_id: int) -> float:
        """Calcule le bonus basé sur les performances aux quiz"""
        
        # Récupérer les quiz du cours via les modules
        user_quiz_results = self.quiz_results[
            self.quiz_results['user_id'] == user_id
        ]
        
        if user_quiz_results.empty:
            return 0.0
        
        # Calculer la moyenne des scores
        avg_score = user_quiz_results['score'].mean()
        
        # Convertir en bonus (0-1)
        if avg_score >= 80:
            return 1.0
        elif avg_score >= 60:
            return 0.5
        elif avg_score >= 40:
            return 0.2
        else:
            return 0.0
    
    def _calculate_time_bonus(self, enrolled_timestamp: int) -> float:
        """Calcule le bonus temporel pour activité récente"""
        
        current_time = datetime.now().timestamp() * 1000
        days_ago = (current_time - enrolled_timestamp) / (1000 * 60 * 60 * 24)
        
        if days_ago <= 7:
            return 0.5
        elif days_ago <= 30:
            return 0.3
        elif days_ago <= 90:
            return 0.1
        else:
            return 0.0
    
    def _extract_content_features(self) -> None:
        """Extrait les features de contenu des cours"""
        
        # Encoder les catégories
        categories = pd.get_dummies(self.cours['categorie'], prefix='cat')
        
        # Features numériques (à adapter selon vos données)
        numeric_features = pd.DataFrame({
            'cours_id': self.cours['id'],
            'formateur_id': self.cours['formateur_id']
        })
        
        # Combiner toutes les features
        self.content_features = pd.concat([
            numeric_features.set_index('cours_id'),
            categories.set_index(self.cours['id'])
        ], axis=1).fillna(0)
        
        logger.info(f"Features de contenu extraites: {self.content_features.shape}")
    
    def train_collaborative_filtering(self) -> None:
        """Entraîne le modèle de filtrage collaboratif avec SVD"""
        
        if self.user_item_matrix is None:
            raise ValueError("Données non chargées. Appelez load_data() d'abord.")
        
        # Normaliser la matrice
        normalized_matrix = self.scaler.fit_transform(self.user_item_matrix)
        
        # Appliquer SVD pour réduction dimensionnelle
        n_components = min(50, min(self.user_item_matrix.shape) - 1)
        self.svd_model = TruncatedSVD(n_components=n_components, random_state=42)
        self.user_factors = self.svd_model.fit_transform(normalized_matrix)
        
        # Calculer la matrice de similarité utilisateur
        self.similarity_matrix = cosine_similarity(self.user_factors)
        
        logger.info("Modèle de filtrage collaboratif entraîné")
    
    def get_collaborative_recommendations(self, user_id: int, n_recommendations: int = 5) -> List[Dict]:
        """
        Génère des recommandations par filtrage collaboratif
        
        Args:
            user_id: ID de l'utilisateur
            n_recommendations: Nombre de recommandations
            
        Returns:
            Liste des recommandations avec scores
        """
        
        if user_id not in self.user_item_matrix.index:
            return []
        
        user_idx = list(self.user_item_matrix.index).index(user_id)
        user_similarities = self.similarity_matrix[user_idx]
        
        # Trouver les utilisateurs similaires
        similar_users_idx = np.argsort(user_similarities)[::-1][1:11]  # Top 10 similaires
        
        # Cours déjà suivis par l'utilisateur
        user_courses = set(self.user_item_matrix.columns[
            self.user_item_matrix.iloc[user_idx] > 0
        ])
        
        # Recommandations basées sur utilisateurs similaires
        recommendations = {}
        
        for similar_idx in similar_users_idx:
            similar_user_id = self.user_item_matrix.index[similar_idx]
            similarity_score = user_similarities[similar_idx]
            
            # Cours suivis par l'utilisateur similaire
            similar_user_courses = self.user_item_matrix.iloc[similar_idx]
            
            for cours_id, rating in similar_user_courses.items():
                if rating > 0 and cours_id not in user_courses:
                    if cours_id not in recommendations:
                        recommendations[cours_id] = 0
                    recommendations[cours_id] += rating * similarity_score
        
        # Trier et retourner top N
        sorted_recommendations = sorted(
            recommendations.items(), 
            key=lambda x: x[1], 
            reverse=True
        )[:n_recommendations]
        
        return [
            {
                'cours_id': cours_id,
                'score': score,
                'type': 'collaborative'
            }
            for cours_id, score in sorted_recommendations
        ]
    
    def get_content_based_recommendations(self, user_id: int, n_recommendations: int = 5) -> List[Dict]:
        """
        Génère des recommandations basées sur le contenu
        
        Args:
            user_id: ID de l'utilisateur
            n_recommendations: Nombre de recommandations
            
        Returns:
            Liste des recommandations avec scores
        """
        
        if user_id not in self.user_item_matrix.index:
            return []
        
        # Cours déjà suivis par l'utilisateur
        user_courses = self.user_item_matrix.loc[user_id]
        completed_courses = user_courses[user_courses > 0].index.tolist()
        
        if not completed_courses:
            return []
        
        # Calculer le profil utilisateur basé sur les cours complétés
        user_profile = np.zeros(self.content_features.shape[1])
        
        for cours_id in completed_courses:
            if cours_id in self.content_features.index:
                weight = user_courses[cours_id]
                user_profile += weight * self.content_features.loc[cours_id].values
        
        user_profile = user_profile / len(completed_courses)
        
        # Calculer similarité avec tous les cours
        similarities = {}
        
        for cours_id in self.content_features.index:
            if cours_id not in completed_courses:
                course_features = self.content_features.loc[cours_id].values
                similarity = cosine_similarity([user_profile], [course_features])[0][0]
                similarities[cours_id] = similarity
        
        # Trier et retourner top N
        sorted_recommendations = sorted(
            similarities.items(), 
            key=lambda x: x[1], 
            reverse=True
        )[:n_recommendations]
        
        return [
            {
                'cours_id': cours_id,
                'score': score,
                'type': 'content_based'
            }
            for cours_id, score in sorted_recommendations
        ]
    
    def get_level_based_recommendations(self, user_id: int, n_recommendations: int = 3) -> List[Dict]:
        """
        Recommandations basées sur le niveau de l'utilisateur
        
        Args:
            user_id: ID de l'utilisateur
            n_recommendations: Nombre de recommandations
            
        Returns:
            Liste des recommandations adaptées au niveau
        """
        
        # Récupérer le niveau de l'utilisateur
        user_level_info = self.user_xp[self.user_xp['user_id'] == user_id]
        
        if user_level_info.empty:
            current_level = 1
        else:
            current_level = user_level_info.iloc[0]['current_level']
        
        # Cours déjà suivis
        if user_id in self.user_item_matrix.index:
            completed_courses = set(self.user_item_matrix.columns[
                self.user_item_matrix.loc[user_id] > 0
            ])
        else:
            completed_courses = set()
        
        # Logique de recommandation par niveau
        recommendations = []
        
        # Cours de base pour débutants (niveau 1-2)
        if current_level <= 2:
            basic_categories = ['Fondamentaux', 'Introduction', 'Débutant']
            for cat in basic_categories:
                matching_courses = self.cours[
                    (self.cours['categorie'].str.contains(cat, case=False, na=False)) &
                    (~self.cours['id'].isin(completed_courses))
                ]
                
                for _, course in matching_courses.head(2).iterrows():
                    recommendations.append({
                        'cours_id': course['id'],
                        'score': 0.9,
                        'type': 'level_based',
                        'reason': f'Cours adapté au niveau {current_level}'
                    })
        
        # Cours intermédiaires (niveau 3-5)
        elif current_level <= 5:
            intermediate_categories = ['Intermédiaire', 'Pratique', 'Application']
            for cat in intermediate_categories:
                matching_courses = self.cours[
                    (self.cours['categorie'].str.contains(cat, case=False, na=False)) &
                    (~self.cours['id'].isin(completed_courses))
                ]
                
                for _, course in matching_courses.head(2).iterrows():
                    recommendations.append({
                        'cours_id': course['id'],
                        'score': 0.85,
                        'type': 'level_based',
                        'reason': f'Cours intermédiaire pour niveau {current_level}'
                    })
        
        # Cours avancés (niveau 6+)
        else:
            advanced_categories = ['Avancé', 'Expert', 'Spécialisé', 'Maîtrise']
            for cat in advanced_categories:
                matching_courses = self.cours[
                    (self.cours['categorie'].str.contains(cat, case=False, na=False)) &
                    (~self.cours['id'].isin(completed_courses))
                ]
                
                for _, course in matching_courses.head(2).iterrows():
                    recommendations.append({
                        'cours_id': course['id'],
                        'score': 0.95,
                        'type': 'level_based',
                        'reason': f'Cours avancé pour niveau {current_level}'
                    })
        
        return recommendations[:n_recommendations]
    
    def generate_hybrid_recommendations(self, user_id: int, n_recommendations: int = 10) -> Dict:
        """
        Génère des recommandations hybrides combinant toutes les approches
        
        Args:
            user_id: ID de l'utilisateur
            n_recommendations: Nombre total de recommandations
            
        Returns:
            Dictionnaire avec recommandations formatées en JSON
        """
        
        # Obtenir recommandations de chaque méthode
        collaborative_recs = self.get_collaborative_recommendations(user_id, 4)
        content_recs = self.get_content_based_recommendations(user_id, 4)
        level_recs = self.get_level_based_recommendations(user_id, 2)
        
        # Combiner et pondérer les recommandations
        all_recommendations = {}
        
        # Pondération: Collaboratif (40%), Contenu (40%), Niveau (20%)
        weights = {
            'collaborative': 0.4,
            'content_based': 0.4,
            'level_based': 0.2
        }
        
        for recs, method in [(collaborative_recs, 'collaborative'), 
                           (content_recs, 'content_based'), 
                           (level_recs, 'level_based')]:
            
            for rec in recs:
                cours_id = rec['cours_id']
                score = rec['score'] * weights[method]
                
                if cours_id not in all_recommendations:
                    all_recommendations[cours_id] = {
                        'cours_id': cours_id,
                        'total_score': 0,
                        'methods': [],
                        'reasons': []
                    }
                
                all_recommendations[cours_id]['total_score'] += score
                all_recommendations[cours_id]['methods'].append(method)
                
                if 'reason' in rec:
                    all_recommendations[cours_id]['reasons'].append(rec['reason'])
        
        # Trier par score total
        sorted_recs = sorted(
            all_recommendations.values(),
            key=lambda x: x['total_score'],
            reverse=True
        )[:n_recommendations]
        
        # Formater les recommandations finales
        final_recommendations = []
        
        for i, rec in enumerate(sorted_recs):
            cours_id = rec['cours_id']
            
            # Récupérer les détails du cours
            course_info = self.cours[self.cours['id'] == cours_id]
            
            if not course_info.empty:
                course = course_info.iloc[0]
                
                # Générer la raison pédagogique
                reason = self._generate_pedagogical_reason(
                    user_id, cours_id, rec['methods'], rec['reasons']
                )
                
                final_recommendations.append({
                    "type": "COURS",
                    "id": int(cours_id),
                    "title": course['titre'],
                    "reason": reason,
                    "priority": i + 1,
                    "confidenceScore": min(0.99, rec['total_score'])
                })
        
        # Retourner au format JSON requis
        return {
            "userId": user_id,
            "generatedAt": datetime.now().isoformat(),
            "recommendations": final_recommendations
        }
    
    def _generate_pedagogical_reason(self, user_id: int, cours_id: int, 
                                   methods: List[str], reasons: List[str]) -> str:
        """Génère une explication pédagogique pour la recommandation"""
        
        # Récupérer le niveau de l'utilisateur
        user_level_info = self.user_xp[self.user_xp['user_id'] == user_id]
        current_level = user_level_info.iloc[0]['current_level'] if not user_level_info.empty else 1
        
        # Récupérer les performances récentes
        recent_quiz_avg = self._get_recent_quiz_performance(user_id)
        
        # Générer raison basée sur la méthode principale
        if 'collaborative' in methods and 'content_based' in methods:
            base_reason = "Recommandé par des utilisateurs similaires ayant des intérêts proches"
        elif 'collaborative' in methods:
            base_reason = "Apprécié par des utilisateurs avec un profil similaire au vôtre"
        elif 'content_based' in methods:
            base_reason = "Correspond à vos préférences de contenu"
        elif 'level_based' in methods:
            base_reason = f"Adapté à votre niveau actuel ({current_level})"
        else:
            base_reason = "Recommandé pour votre progression"
        
        # Ajouter contexte pédagogique
        if recent_quiz_avg >= 80:
            context = " - Excellent pour continuer sur votre lancée"
        elif recent_quiz_avg >= 60:
            context = " - Idéal pour renforcer vos acquis"
        elif recent_quiz_avg < 50 and recent_quiz_avg > 0:
            context = " - Recommandé pour consolider vos bases"
        else:
            context = " - Parfait pour débuter votre apprentissage"
        
        return base_reason + context
    
    def _get_recent_quiz_performance(self, user_id: int) -> float:
        """Calcule la performance moyenne aux quiz récents"""
        
        user_quizzes = self.quiz_results[self.quiz_results['user_id'] == user_id]
        
        if user_quizzes.empty:
            return 0.0
        
        # Prendre les 5 derniers quiz
        recent_quizzes = user_quizzes.nlargest(5, 'date_passed')
        return recent_quizzes['score'].mean()


def example_usage():
    """
    Exemple d'utilisation du moteur de recommandation
    """
    
    # Données d'exemple (à remplacer par vos vraies données)
    enrollments_data = {
        'user_id': [1, 1, 2, 2, 3, 3, 4, 4, 5],
        'cours_id': [101, 102, 101, 103, 102, 104, 101, 105, 103],
        'progress': [85.0, 60.0, 95.0, 40.0, 100.0, 75.0, 50.0, 90.0, 80.0],
        'enrolled_at': [1640995200000, 1641081600000, 1641168000000, 
                       1641254400000, 1641340800000, 1641427200000,
                       1641513600000, 1641600000000, 1641686400000]
    }
    
    quiz_results_data = {
        'user_id': [1, 1, 2, 2, 3, 4, 5],
        'quiz_id': [201, 202, 201, 203, 202, 201, 203],
        'score': [85.0, 70.0, 95.0, 60.0, 90.0, 75.0, 80.0],
        'date_passed': [1640995200000, 1641081600000, 1641168000000,
                       1641254400000, 1641340800000, 1641427200000, 1641513600000]
    }
    
    cours_data = {
        'id': [101, 102, 103, 104, 105, 106, 107],
        'titre': ['Python Fondamentaux', 'JavaScript Avancé', 'Data Science', 
                 'Machine Learning', 'Web Development', 'DevOps', 'Mobile Development'],
        'categorie': ['Programmation', 'Programmation', 'Data', 'IA', 'Web', 'Infrastructure', 'Mobile'],
        'formateur_id': [10, 11, 12, 13, 14, 15, 16]
    }
    
    user_xp_data = {
        'user_id': [1, 2, 3, 4, 5],
        'total_xp': [450, 800, 1200, 300, 600],
        'current_level': [2, 3, 4, 1, 3]
    }
    
    # Créer les DataFrames
    enrollments_df = pd.DataFrame(enrollments_data)
    quiz_results_df = pd.DataFrame(quiz_results_data)
    cours_df = pd.DataFrame(cours_data)
    user_xp_df = pd.DataFrame(user_xp_data)
    
    # Initialiser et entraîner le moteur
    engine = RecommendationEngine()
    engine.load_data(enrollments_df, quiz_results_df, cours_df, user_xp_df)
    engine.train_collaborative_filtering()
    
    # Générer des recommandations pour l'utilisateur 1
    recommendations = engine.generate_hybrid_recommendations(user_id=1, n_recommendations=5)
    
    # Afficher le résultat JSON
    print(json.dumps(recommendations, indent=2, ensure_ascii=False))
    
    return recommendations


if __name__ == "__main__":
    # Exécuter l'exemple
    result = example_usage()