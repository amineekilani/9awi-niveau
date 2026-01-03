import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CoursService, Cours } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { AuthService } from '../auth';
import { UserGamificationService, RecentActivity } from '../user-gamification.service';
import { GamificationNotificationService } from '../gamification-notification.service';

declare const feather: any;

interface CoursWithEnrollment extends Cours {
  enrollment?: Enrollment;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-mes-cours',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './mes-cours.html',
  styleUrls: ['./mes-cours.css']
})
export class MesCoursComponent implements OnInit, AfterViewInit {
  user: any = null;
  sidebarOpen = false;

  // Données utilisateur
  userInitials = 'ET';
  userProfileImage = '';

  // Notifications
  showNotifications = false;
  recentActivity: RecentActivity[] = [];

  // Cours Data
  cours: CoursWithEnrollment[] = [];
  filteredCours: CoursWithEnrollment[] = [];
  loading = true;
  error = '';

  // Filtres spécifiques aux cours inscrits
  selectedFilter = 'all';
  filterOptions = [
    { value: 'all', label: 'Tous mes cours', count: 0 },
    { value: 'in-progress', label: 'En cours', count: 0 },
    { value: 'completed', label: 'Terminés', count: 0 }
  ];

  // Statistiques
  totalEnrolled = 0;
  completedCount = 0;
  inProgressCount = 0;
  averageProgress = 0;

  constructor(
    private router: Router,
    public authService: AuthService,
    private coursService: CoursService,
    private enrollmentService: EnrollmentService,
    private userGamificationService: UserGamificationService,
    private notificationService: GamificationNotificationService
  ) { }

  ngOnInit() {
    // this.authService.currentUser n'existe pas, on utilise userProfile$ plus bas


    // Charger le profil pour l'image
    if (this.authService.getToken()) {
      this.authService.loadUserProfile();
    }
    this.authService.userProfile$.subscribe(profile => {
      if (profile) {
        this.userProfileImage = profile.profileImage || '';
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';
        if (firstName && lastName) {
          this.userInitials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        } else if (profile.email) {
          const parts = profile.email.split('@')[0].split('.');
          this.userInitials = parts.map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
      }
    });

    this.loadMesCours();

    // Notifications logic
    this.notificationService.checkForNewAchievements();
    this.loadNotifications();

    document.addEventListener('click', (event: any) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.notification-container')) {
        this.showNotifications = false;
      }
    });
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  loadMesCours() {
    this.coursService.getAllCours().subscribe({
      next: (allCours) => {
        this.enrollmentService.getUserEnrollments().subscribe({
          next: (enrollments) => {
            // Filtrer uniquement les cours inscrits
            this.cours = allCours
              .filter(c => enrollments.some(e => e.coursId === c.id))
              .map(c => {
                const enrollment = enrollments.find(e => e.coursId === c.id);
                return {
                  ...c,
                  enrollment: enrollment,
                  isEnrolled: true
                };
              });

            this.updateStats();
            this.updateFilterCounts();
            this.applyFilter();
            this.loading = false;

            setTimeout(() => {
              if (typeof feather !== 'undefined') feather.replace();
            }, 100);
          },
          error: (err) => {
            this.error = 'Erreur lors du chargement des inscriptions';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
      }
    });
  }

  updateStats() {
    this.totalEnrolled = this.cours.length;
    this.completedCount = this.cours.filter(c => c.enrollment?.progress === 100).length;
    this.inProgressCount = this.totalEnrolled - this.completedCount;

    const totalProgress = this.cours.reduce((sum, c) => sum + (c.enrollment?.progress || 0), 0);
    this.averageProgress = this.totalEnrolled > 0 ? Math.round(totalProgress / this.totalEnrolled) : 0;
  }

  updateFilterCounts() {
    this.filterOptions.forEach(opt => {
      if (opt.value === 'all') opt.count = this.cours.length;
      if (opt.value === 'in-progress') opt.count = this.inProgressCount;
      if (opt.value === 'completed') opt.count = this.completedCount;
    });
  }

  setFilter(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  applyFilter() {
    if (this.selectedFilter === 'all') {
      this.filteredCours = [...this.cours];
    } else if (this.selectedFilter === 'in-progress') {
      this.filteredCours = this.cours.filter(c => (c.enrollment?.progress || 0) < 100);
    } else if (this.selectedFilter === 'completed') {
      this.filteredCours = this.cours.filter(c => c.enrollment?.progress === 100);
    }

    // Refresh icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') feather.replace();
    }, 50);
  }

  continueCourse(coursId: number) {
    this.router.navigate(['/cours', coursId]);
  }

  logout() {
    this.authService.logout();
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  // --- Notifications Logic ---

  loadNotifications() {
    this.userGamificationService.getRecentActivity(5).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
      }
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      setTimeout(() => feather.replace(), 100);
    }
  }
  onFilterChange(filter: string) {
    this.setFilter(filter);
  }

  getProgressText(progress: number): string {
    if (progress === 100) return 'Terminé';
    if (progress > 0) return 'En cours';
    return 'Non commencé';
  }

  getProgressColor(progress: number): string {
    if (progress === 100) return 'bg-green-100 text-green-800';
    if (progress > 0) return 'bg-yellow-100 text-yellow-800';
    return 'bg-gray-100 text-gray-800';
  }
}