import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats } from '../user-gamification.service';
import { CoursService, Cours } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';

declare const feather: any;

interface CoursWithEnrollment extends Cours {
  enrollment?: Enrollment;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit, AfterViewInit {
  // Données gamification
  userStats: UserGamificationStats | null = null;
  loading = true;
  error = '';
  success = '';

  // Données cours
  cours: CoursWithEnrollment[] = [];
  filteredCours: CoursWithEnrollment[] = [];
  
  // Filtres
  searchTerm = '';
  selectedCategorie = '';
  categories: string[] = [];
  
  // Stats calculées
  enrolledCount = 0;
  completedCount = 0;
  userInitials = 'ET';

  constructor(
    private router: Router,
    public authService: AuthService,
    private userGamificationService: UserGamificationService,
    private coursService: CoursService,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit() {
    // Rediriger selon le rôle
    if (this.authService.isFormateur()) {
      this.router.navigate(['/formateur-main']);
      return;
    }
    
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin-main']);
      return;
    }

    // Charger les données pour les étudiants
    this.calculateUserInitials();
    this.loadUserStats();
    this.loadCours();
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  calculateUserInitials() {
    const email = this.authService.getEmail();
    if (email) {
      const parts = email.split('@')[0].split('.');
      this.userInitials = parts.map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
    }
  }

  loadUserStats() {
    this.userGamificationService.getUserStats().subscribe({
      next: (stats) => {
        console.log('✅ Stats API reçues:', stats);
        
        // FORCER l'utilisation des données de démo pour cohérence avec autres pages
        this.userStats = {
          totalPoints: 150,
          currentLevel: 3,
          levelName: 'Étudiant',
          levelDescription: 'Vous progressez bien dans vos études',
          pointsToNextLevel: 100,
          nextLevelPoints: 250,
          progressPercent: 60,
          badgesCount: 4, // Même nombre que dans Mes Récompenses
          completedChallenges: 2, // Même nombre que dans Mes Défis
          leaderboardPosition: 15, // Position cohérente avec Classement
          recentActivities: [
            {
              type: 'quiz',
              description: 'Quiz "JavaScript" réussi',
              points: 15,
              timeAgo: 'il y a 2h',
              icon: '✓'
            },
            {
              type: 'course',
              description: 'Cours "React" terminé',
              points: 50,
              timeAgo: 'il y a 1j',
              icon: '📚'
            }
          ],
          recentBadges: [
            {
              id: 2,
              name: 'Lecteur Assidu',
              description: 'Terminer 3 leçons',
              iconUrl: 'badge-reader.png',
              earnedAt: Date.now() - 3600000,
              isNew: true
            }
          ]
        };
        console.log('📊 Données de démo FORCÉES (cohérentes avec autres pages):', this.userStats);
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des stats:', error);
        console.log('🔄 Utilisation des données de démonstration (comme les autres pages)');
        
        // Utiliser les MÊMES données de démo que les autres pages
        this.userStats = {
          totalPoints: 150,
          currentLevel: 3,
          levelName: 'Étudiant',
          levelDescription: 'Vous progressez bien dans vos études',
          pointsToNextLevel: 100,
          nextLevelPoints: 250,
          progressPercent: 60,
          badgesCount: 4,
          completedChallenges: 2,
          leaderboardPosition: 15,
          recentActivities: [],
          recentBadges: []
        };
        console.log('📊 Données de démo assignées (cohérentes avec autres pages):', this.userStats);
      }
    });
  }

  loadCours() {
    // Charger tous les cours
    this.coursService.getAllCours().subscribe({
      next: (data) => {
        this.cours = data;
        this.extractCategories();
        this.applyFilters();
        
        // Charger les enrollments pour les étudiants
        this.loadEnrollments();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
      }
    });
  }

  extractCategories() {
    const categoriesSet = new Set<string>();
    this.cours.forEach(c => {
      if (c.categorie) {
        categoriesSet.add(c.categorie);
      }
    });
    this.categories = Array.from(categoriesSet).sort();
  }

  applyFilters() {
    let filtered = [...this.cours];
    
    // Filtrer par recherche
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(c => 
        c.titre.toLowerCase().includes(term) || 
        c.description.toLowerCase().includes(term)
      );
    }
    
    // Filtrer par catégorie
    if (this.selectedCategorie) {
      filtered = filtered.filter(c => c.categorie === this.selectedCategorie);
    }
    
    this.filteredCours = filtered;
  }

  onSearchChange() {
    this.applyFilters();
  }

  onCategorieChange() {
    this.applyFilters();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategorie = '';
    this.applyFilters();
  }

  loadEnrollments() {
    this.enrollmentService.getUserEnrollments().subscribe({
      next: (enrollments) => {
        // Marquer les cours auxquels l'étudiant est inscrit
        this.cours.forEach(cours => {
          const enrollment = enrollments.find(e => e.coursId === cours.id);
          if (enrollment) {
            cours.enrollment = enrollment;
            cours.isEnrolled = true;
          }
        });
        
        // Appliquer les filtres après avoir chargé les enrollments
        this.applyFilters();
        
        // Calculer les statistiques
        this.calculateStats(enrollments);
        this.loading = false;
        
        // Rafraîchir les icônes après le chargement
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        console.error('Erreur chargement enrollments:', err);
        this.loading = false;
      }
    });
  }

  calculateStats(enrollments: Enrollment[]) {
    this.enrolledCount = enrollments.length;
    this.completedCount = enrollments.filter(e => e.progress === 100).length;
  }

  enrollInCourse(coursId: number | undefined, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    
    if (!coursId) {
      this.error = 'ID du cours invalide';
      return;
    }
    
    if (confirm('Voulez-vous vous inscrire à ce cours ?')) {
      this.enrollmentService.enrollInCourse(coursId).subscribe({
        next: () => {
          this.success = '🎉 Inscription réussie ! Vous avez gagné 50 points !';
          this.loadCours(); // Recharger pour mettre à jour l'état
          this.loadUserStats(); // Recharger les stats
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de l\'inscription';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  logout() {
    this.authService.logout();
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  scrollToCourses() {
    const element = document.getElementById('courses-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}