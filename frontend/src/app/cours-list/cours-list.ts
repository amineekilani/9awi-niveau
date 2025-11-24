import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CoursService, Cours } from '../cours.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { AuthService } from '../auth';

interface CoursWithEnrollment extends Cours {
  enrollment?: Enrollment;
  isEnrolled?: boolean;
}

@Component({
  selector: 'app-cours-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cours-list.html',
  styleUrls: ['./cours-list.css']
})
export class CoursListComponent implements OnInit {
  cours: CoursWithEnrollment[] = [];
  loading = false;
  error = '';
  success = '';

  constructor(
    private coursService: CoursService,
    private enrollmentService: EnrollmentService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadCours();
  }

  loadCours() {
    this.loading = true;
    
    // Charger tous les cours
    this.coursService.getAllCours().subscribe({
      next: (data) => {
        this.cours = data;
        
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
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement enrollments:', err);
        this.loading = false;
      }
    });
  }

  enrollInCourse(coursId: number, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    
    if (confirm('Voulez-vous vous inscrire à ce cours ?')) {
      this.enrollmentService.enrollInCourse(coursId).subscribe({
        next: () => {
          this.success = 'Inscription réussie !';
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
}
