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
  cours: Cours[] = [];
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

  loadCours() {
    this.loading = true;
    this.coursService.getMesCours().subscribe({
      next: (data) => {
        this.cours = data;
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

  logout() {
    this.authService.logout();
  }
}
