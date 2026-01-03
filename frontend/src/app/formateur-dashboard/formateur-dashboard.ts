import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth';
import { CoursService, Cours } from '../cours.service';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';

declare const feather: any;

@Component({
  selector: 'app-formateur-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './formateur-dashboard.html',
  styleUrls: ['./formateur-dashboard.css']
})
export class FormateurDashboardComponent implements OnInit {
  allCours: Cours[] = [];
  activeTab: 'actifs' | 'archives' = 'actifs';
  loading = false;
  error = '';

  // Données pour le header unifié
  userInitials = 'ET';
  userProfileImage = '';
  showNotifications = false;
  recentActivity: RecentActivity[] = [];
  userStats: UserGamificationStats | null = null;

  constructor(
    private coursService: CoursService,
    public authService: AuthService,
    private gamificationService: UserGamificationService,
    private router: Router
  ) { }

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    // Initialiser les données du header
    this.initHeaderData();
    this.loadCours();
  }

  private initHeaderData() {
    this.authService.userProfile$.subscribe(profile => {
      if (profile) {
        this.userProfileImage = profile.profileImage || '';
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';
        if (firstName && lastName) {
          this.userInitials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        } else if (profile.email) {
          const namePart = profile.email.split('@')[0];
          this.userInitials = namePart.split('.').map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
      }
    });

    if (this.authService.getToken() && !this.userProfileImage) {
      this.authService.loadUserProfile();
    }

    this.gamificationService.getRecentActivity(5).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
        setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
      }
    });

    this.gamificationService.getUserStats().subscribe({
      next: (stats) => this.userStats = stats
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
    }
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  get coursActifs(): Cours[] {
    return this.allCours.filter(c => !c.archived);
  }

  get coursArchives(): Cours[] {
    return this.allCours.filter(c => c.archived);
  }

  get displayedCours(): Cours[] {
    return this.activeTab === 'actifs' ? this.coursActifs : this.coursArchives;
  }

  switchTab(tab: 'actifs' | 'archives') {
    this.activeTab = tab;
  }

  loadCours() {
    this.loading = true;
    this.error = '';
    this.coursService.getMesCours().subscribe({
      next: (data) => {
        console.log('Cours reçus du backend:', data);
        this.allCours = data;
        console.log('Cours actifs:', this.coursActifs);
        console.log('Cours archivés:', this.coursArchives);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
      }
    });
  }

  archiveCours(id: number) {
    if (confirm('Êtes-vous sûr de vouloir archiver ce cours ?')) {
      this.coursService.archiveCours(id).subscribe({
        next: () => {
          this.loadCours();
        },
        error: (err) => {
          this.error = 'Erreur lors de l\'archivage du cours';
        }
      });
    }
  }

  unarchiveCours(id: number) {
    if (confirm('Êtes-vous sûr de vouloir réactiver ce cours ?')) {
      this.coursService.unarchiveCours(id).subscribe({
        next: () => {
          this.loadCours();
        },
        error: (err) => {
          this.error = 'Erreur lors de la réactivation du cours';
        }
      });
    }
  }

  logout() {
    this.authService.logout();
  }
}
