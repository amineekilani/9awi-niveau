import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';
import { CoursService, Cours, NiveauDifficulte, NiveauDifficulteInfo } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { GamificationNotificationService } from '../gamification-notification.service';
import { NiveauBadgeComponent } from '../niveau-badge/niveau-badge';

declare const feather: any;

interface CoursWithEnrollment extends Cours {
  enrollment?: Enrollment;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NavbarComponent, NiveauBadgeComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit, AfterViewInit {
  sidebarOpen = false;

  // Données gamification
  userStats: UserGamificationStats | null = null;
  loading = true;
  error = '';
  success = '';

  // Cours
  cours: CoursWithEnrollment[] = [];
  filteredCours: CoursWithEnrollment[] = [];

  // Filtres
  searchTerm = '';
  selectedCategorie = '';
  selectedNiveau = '';
  categories: string[] = [];
  niveauxDifficulte: NiveauDifficulteInfo[] = [];

  // Stats calculées
  enrolledCount = 0;
  completedCount = 0;
  // Notifications
  recentActivity: RecentActivity[] = [];

  constructor(
    private router: Router,
    public authService: AuthService,
    private userGamificationService: UserGamificationService,
    private coursService: CoursService,
    private enrollmentService: EnrollmentService,
    private notificationService: GamificationNotificationService
  ) { }

  ngOnInit() {
    // Check for new badges (SweetAlert)
    this.notificationService.checkForNewAchievements();

    this.loadUserStats();
    this.loadCours();
    this.loadNiveauxDifficulte();
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  loadUserStats() {
    console.log('🔄 Début du chargement des stats utilisateur...');
    // this.loading = true; // On laisse le loading global géré par loadCours pour l'instant ou on le gère ici

    this.userGamificationService.getUserStats().subscribe({
      next: (stats) => {
        console.log('✅ Stats utilisateur chargées:', stats);

        if (!stats) {
          this.setFallbackStats();
          return;
        }

        this.userStats = {
          totalPoints: stats.totalPoints || 0,
          currentLevel: stats.currentLevel || 1,
          levelName: stats.levelName || 'Débutant',
          levelDescription: stats.levelDescription || 'Bienvenue !',
          pointsToNextLevel: stats.pointsToNextLevel || 100,
          nextLevelPoints: stats.nextLevelPoints || 100,
          progressPercent: stats.progressPercent || 0,
          badgesCount: stats.badgesCount || 0,
          completedChallenges: stats.completedChallenges || 0,
          leaderboardPosition: stats.leaderboardPosition || 0,
          recentActivities: stats.recentActivities || [],
          recentBadges: stats.recentBadges || []
        };

        // this.loading = false;
        console.log('🎯 Points finaux:', this.userStats.totalPoints);
      },
      error: (error) => {
        console.error('❌ Erreur stats:', error);
        this.setFallbackStats();
      }
    });
  }

  private setFallbackStats() {
    this.userStats = {
      totalPoints: 0,
      currentLevel: 1,
      levelName: 'Débutant',
      levelDescription: 'Bienvenue !',
      pointsToNextLevel: 100,
      nextLevelPoints: 100,
      progressPercent: 0,
      badgesCount: 0,
      completedChallenges: 0,
      leaderboardPosition: 0,
      recentActivities: [],
      recentBadges: []
    };
  }

  loadCours() {
    this.coursService.getAllCours().subscribe({
      next: (data) => {
        this.cours = data;
        this.extractCategories();
        this.applyFilters();
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

  loadNiveauxDifficulte() {
    this.coursService.getNiveauxDifficulte().subscribe({
      next: (niveaux) => {
        this.niveauxDifficulte = niveaux;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des niveaux:', err);
      }
    });
  }

  applyFilters() {
    let filtered = [...this.cours];

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(c =>
        c.titre.toLowerCase().includes(term) ||
        c.description.toLowerCase().includes(term) ||
        (c.keywords && c.keywords.toLowerCase().includes(term))
      );
    }

    if (this.selectedCategorie) {
      filtered = filtered.filter(c => c.categorie === this.selectedCategorie);
    }

    if (this.selectedNiveau) {
      filtered = filtered.filter(c => c.niveauDifficulte === this.selectedNiveau);
    }

    this.filteredCours = filtered;
  }

  onSearchChange() {
    this.applyFilters();
  }

  onCategorieChange() {
    this.applyFilters();
  }

  onNiveauChange() {
    this.applyFilters();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategorie = '';
    this.selectedNiveau = '';
    this.applyFilters();
  }

  loadEnrollments() {
    this.enrollmentService.getUserEnrollments().subscribe({
      next: (enrollments) => {
        this.cours.forEach(cours => {
          const enrollment = enrollments.find(e => e.coursId === cours.id);
          if (enrollment) {
            cours.enrollment = enrollment;
            cours.isEnrolled = true;
          }
        });

        this.applyFilters();
        this.calculateStats(enrollments);
        this.loading = false;

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
          this.loadCours();
          this.loadUserStats();
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
    document.getElementById('courses-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  getNiveauDisplayName(niveau: string): string {
    const niveauInfo = this.niveauxDifficulte.find(n => n.niveau === niveau);
    return niveauInfo ? niveauInfo.displayName : niveau;
  }

  toggleNotifications() {
    // Handled by Navbar
  }
}