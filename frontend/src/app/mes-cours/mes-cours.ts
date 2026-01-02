import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CoursService, Cours } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { AuthService } from '../auth';

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
  cours: CoursWithEnrollment[] = [];
  filteredCours: CoursWithEnrollment[] = [];
  loading = true;
  error = '';
  userInitials = 'ET';
  userName = '';

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
    private coursService: CoursService,
    private enrollmentService: EnrollmentService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.calculateUserInitials();
    this.loadMesCours();
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

  loadMesCours() {
    this.loading = true;

    // Charger les enrollments de l'utilisateur
    this.enrollmentService.getUserEnrollments().subscribe({
      next: (enrollments) => {
        if (enrollments.length === 0) {
          this.cours = [];
          this.filteredCours = [];
          this.loading = false;
          return;
        }

        // Charger les détails des cours inscrits
        const coursIds = enrollments.map(e => e.coursId);
        this.loadCoursDetails(coursIds, enrollments);
      },
      error: (err) => {
        console.error('Erreur chargement enrollments:', err);
        this.error = 'Erreur lors du chargement de vos cours';
        this.loading = false;
      }
    });
  }

  loadCoursDetails(coursIds: number[], enrollments: Enrollment[]) {
    // Charger tous les cours pour filtrer ceux inscrits
    this.coursService.getAllCours().subscribe({
      next: (allCours) => {
        // Filtrer seulement les cours inscrits
        this.cours = allCours
          .filter(cours => coursIds.includes(cours.id!))
          .map(cours => {
            const enrollment = enrollments.find(e => e.coursId === cours.id);
            return {
              ...cours,
              enrollment,
              isEnrolled: true
            };
          });

        this.calculateStatistics();
        this.updateFilterCounts();
        this.applyFilter();
        this.loading = false;

        // Rafraîchir les icônes
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        console.error('Erreur chargement cours:', err);
        this.error = 'Erreur lors du chargement des détails des cours';
        this.loading = false;
      }
    });
  }

  calculateStatistics() {
    this.totalEnrolled = this.cours.length;
    this.completedCount = this.cours.filter(c => c.enrollment?.progress === 100).length;
    this.inProgressCount = this.cours.filter(c => c.enrollment && c.enrollment.progress > 0 && c.enrollment.progress < 100).length;

    if (this.cours.length > 0) {
      const totalProgress = this.cours.reduce((sum, c) => sum + (c.enrollment?.progress || 0), 0);
      this.averageProgress = Math.round(totalProgress / this.cours.length);
    }
  }

  updateFilterCounts() {
    const inProgress = this.cours.filter(c => c.enrollment && c.enrollment.progress > 0 && c.enrollment.progress < 100);
    const completed = this.cours.filter(c => c.enrollment?.progress === 100);

    this.filterOptions[0].count = this.cours.length;
    this.filterOptions[1].count = inProgress.length;
    this.filterOptions[2].count = completed.length;
  }

  applyFilter() {
    switch (this.selectedFilter) {
      case 'in-progress':
        this.filteredCours = this.cours.filter(c =>
          c.enrollment && c.enrollment.progress > 0 && c.enrollment.progress < 100
        );
        break;
      case 'completed':
        this.filteredCours = this.cours.filter(c => c.enrollment?.progress === 100);
        break;
      default:
        this.filteredCours = [...this.cours];
    }
  }

  onFilterChange(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  getProgressColor(progress: number): string {
    if (progress === 100) return 'bg-green-500';
    if (progress >= 50) return 'bg-blue-500';
    if (progress > 0) return 'bg-yellow-500';
    return 'bg-gray-300';
  }

  getProgressText(progress: number): string {
    if (progress === 100) return 'Terminé';
    if (progress > 0) return 'En cours';
    return 'Non commencé';
  }

  logout() {
    this.authService.logout();
  }
}