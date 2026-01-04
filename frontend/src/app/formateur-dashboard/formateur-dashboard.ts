import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { CoursService, Cours, NiveauDifficulte, NiveauDifficulteInfo } from '../cours.service';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';
import { NiveauBadgeComponent } from '../niveau-badge/niveau-badge';

declare const feather: any;

interface NiveauStats {
  niveau: NiveauDifficulte;
  count: number;
  percentage: number;
}

@Component({
  selector: 'app-formateur-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, NiveauBadgeComponent],
  templateUrl: './formateur-dashboard.html',
  styleUrls: ['./formateur-dashboard.css']
})
export class FormateurDashboardComponent implements OnInit {
  allCours: Cours[] = [];
  activeTab: 'actifs' | 'archives' = 'actifs';
  loading = false;
  error = '';
  niveauxStats: NiveauStats[] = [];
  niveauxDifficulte: NiveauDifficulteInfo[] = [];

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
    this.loadNiveauxDifficulte();
    this.loadCours();
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

  private initHeaderData() {
    // Redundant now as NavbarComponent handles this
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

  get coursWithoutKeywords(): number {
    return this.coursActifs.filter(c => !c.keywords || c.keywords.trim() === '').length;
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
        // Debug spécifique pour les thumbnails
        data.forEach(cours => {
          console.log(`Cours "${cours.titre}": thumbnailUrl = "${cours.thumbnailUrl}"`);
        });
        this.allCours = data;
        this.calculateNiveauxStats();
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

  calculateNiveauxStats() {
    const stats = new Map<NiveauDifficulte, number>();
    const coursActifs = this.coursActifs;
    
    // Compter les cours par niveau
    coursActifs.forEach(cours => {
      if (cours.niveauDifficulte) {
        const count = stats.get(cours.niveauDifficulte) || 0;
        stats.set(cours.niveauDifficulte, count + 1);
      }
    });

    // Convertir en tableau avec pourcentages
    this.niveauxStats = Array.from(stats.entries()).map(([niveau, count]) => ({
      niveau,
      count,
      percentage: coursActifs.length > 0 ? Math.round((count / coursActifs.length) * 100) : 0
    }));

    // Trier par ordre de niveau
    this.niveauxStats.sort((a, b) => {
      const order = { 
        [NiveauDifficulte.DEBUTANT]: 1, 
        [NiveauDifficulte.INTERMEDIAIRE]: 2, 
        [NiveauDifficulte.AVANCE]: 3, 
        [NiveauDifficulte.EXPERT]: 4 
      };
      return order[a.niveau] - order[b.niveau];
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

  getRecommendation(): string {
    if (this.niveauxStats.length === 0) return '';
    
    const totalCours = this.coursActifs.length;
    if (totalCours === 0) return 'Créez votre premier cours pour commencer !';
    
    const debutantCount = this.niveauxStats.find(s => s.niveau === NiveauDifficulte.DEBUTANT)?.count || 0;
    const expertCount = this.niveauxStats.find(s => s.niveau === NiveauDifficulte.EXPERT)?.count || 0;
    
    if (debutantCount === 0) {
      return 'Ajoutez des cours débutants pour attirer plus d\'apprenants.';
    }
    
    if (expertCount === 0 && totalCours >= 3) {
      return 'Créez des cours experts pour fidéliser vos apprenants avancés.';
    }
    
    if (totalCours < 5) {
      return 'Diversifiez votre offre en créant des cours de différents niveaux.';
    }
    
    return 'Excellente répartition ! Continuez à équilibrer vos niveaux.';
  }
}
