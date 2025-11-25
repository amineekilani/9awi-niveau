import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CoursService, Cours } from '../cours.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-cours-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cours-form.html',
  styleUrls: ['./cours-form.css']
})
export class CoursFormComponent implements OnInit {
  cours: Cours = {
    titre: '',
    description: '',
    categorie: ''
  };
  isEditMode = false;
  coursId?: number;
  loading = false;
  error = '';
  success = '';

  constructor(
    private coursService: CoursService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    this.coursId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.coursId) {
      this.isEditMode = true;
      this.loadCours();
    }
  }

  loadCours() {
    if (!this.coursId) return;
    
    this.loading = true;
    this.coursService.getCoursById(this.coursId).subscribe({
      next: (data) => {
        this.cours = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du cours';
        this.loading = false;
      }
    });
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.success = '';

    const operation = this.isEditMode
      ? this.coursService.updateCours(this.coursId!, this.cours)
      : this.coursService.createCours(this.cours);

    operation.subscribe({
      next: () => {
        this.success = this.isEditMode ? 'Cours modifié avec succès' : 'Cours créé avec succès';
        setTimeout(() => {
          this.router.navigate(['/formateur-dashboard']);
        }, 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Une erreur est survenue';
        this.loading = false;
      }
    });
  }
}
