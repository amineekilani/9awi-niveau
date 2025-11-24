import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth';
import { CoursService, Cours } from '../cours.service';

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

  constructor(
    private coursService: CoursService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }
    this.loadCours();
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
