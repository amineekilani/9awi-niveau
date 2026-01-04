import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CoursService, Cours, NiveauDifficulte, NiveauDifficulteInfo } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { AuthService } from '../auth';
import { NiveauBadgeComponent } from '../niveau-badge/niveau-badge';

declare const feather: any;

interface CoursWithEnrollment extends Cours {
  enrollment?: Enrollment;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-cours-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NiveauBadgeComponent],
  templateUrl: './cours-list.html',
  styleUrls: ['./cours-list.css']
})
export class CoursListComponent implements OnInit, AfterViewInit {
  cours: CoursWithEnrollment[] = [];
  filteredCours: CoursWithEnrollment[] = [];
  loading = false;
  error = '';
  success = '';

  // Filtres
  searchTerm = '';
  selectedCategorie = '';
  selectedNiveau = '';
  selectedParcours = '';
  categories: string[] = [];
  niveauxDifficulte: NiveauDifficulteInfo[] = [];

  // Statistiques gamifiées
  enrolledCount = 0;
  completedCount = 0;
  totalPoints = 0;
  userLevel = 1;
  overallProgress = 0;
  userInitials = 'ET';

  constructor(
    private coursService: CoursService,
    private enrollmentService: EnrollmentService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadCours();
    this.loadNiveauxDifficulte();
    this.calculateUserInitials();
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

  loadCours() {
    this.loading = true;

    // Charger tous les cours
    this.coursService.getAllCours().subscribe({
      next: (data) => {
        this.cours = data;
        this.extractCategories();
        this.applyFilters();

        // Si étudiant, charger les enrollments
        if (!this.authService.isFormateur()) {
          this.loadEnrollments();
        } else {
          this.loading = false;
        }
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

    // Le filtrage par mot clé est maintenant géré par le backend via onSearchChange
    // On garde juste les filtres catégorie et niveau ici qui s'appliquent sur les résultats retournés
    if (this.selectedCategorie) {
      filtered = filtered.filter(c => c.categorie === this.selectedCategorie);
    }

    if (this.selectedNiveau) {
      filtered = filtered.filter(c => c.niveauDifficulte === this.selectedNiveau);
    }

    // Filtrage par parcours
    if (this.selectedParcours === 'with-parcours') {
      filtered = filtered.filter(c => c.nombreParcours && c.nombreParcours > 0);
    } else if (this.selectedParcours === 'without-parcours') {
      filtered = filtered.filter(c => !c.nombreParcours || c.nombreParcours === 0);
    }

    this.filteredCours = filtered;
  }

  onSearchChange() {
    if (this.searchTerm.trim() || this.selectedCategorie || this.selectedNiveau || this.selectedParcours) {
      this.loading = true;
      this.coursService.searchCours(
        this.searchTerm || undefined, 
        this.selectedCategorie || undefined, 
        this.selectedNiveau as NiveauDifficulte || undefined
      ).subscribe({
        next: (data) => {
          this.cours = data;
          this.applyFilters();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors de la recherche';
          this.loading = false;
        }
      });
    } else {
      // Si tous les filtres sont vides, on recharge tout
      this.loadCours();
    }
  }

  onCategorieChange() {
    this.onSearchChange();
  }

  onNiveauChange() {
    this.onSearchChange();
  }

  onParcoursChange() {
    this.onSearchChange();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategorie = '';
    this.selectedNiveau = '';
    this.selectedParcours = '';
    this.loadCours();
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

    // Calculer la progression globale
    if (enrollments.length > 0) {
      const totalProgress = enrollments.reduce((sum, e) => sum + e.progress, 0);
      this.overallProgress = Math.round(totalProgress / enrollments.length);
    }

    // Calculer les points (10 points par % de progression)
    this.totalPoints = enrollments.reduce((sum, e) => sum + Math.round(e.progress * 10), 0);

    // Calculer le niveau (1 niveau tous les 500 points)
    this.userLevel = Math.floor(this.totalPoints / 500) + 1;
  }

  enrollInCourse(coursId: number, event: Event) {
    event.preventDefault();
    event.stopPropagation();

    if (confirm('Voulez-vous vous inscrire à ce cours ?')) {
      this.enrollmentService.enrollInCourse(coursId).subscribe({
        next: () => {
          this.success = '🎉 Inscription réussie ! Vous avez gagné 50 points !';
          this.loadCours(); // Recharger pour mettre à jour l'état
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

  getNiveauDisplayName(niveau: string): string {
    const niveauInfo = this.niveauxDifficulte.find(n => n.niveau === niveau);
    return niveauInfo ? niveauInfo.displayName : niveau;
  }
}
