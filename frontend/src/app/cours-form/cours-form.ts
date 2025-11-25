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
  selectedFile: File | null = null;
  thumbnailPreview: string | null = null;
  uploadingThumbnail = false;

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
        if (this.cours.thumbnailUrl) {
          this.thumbnailPreview = `http://localhost:8080/images/cours/${this.cours.thumbnailUrl}`;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du cours';
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Vérifier que c'est une image
      if (!file.type.startsWith('image/')) {
        this.error = 'Veuillez sélectionner une image';
        return;
      }

      // Vérifier la taille (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'L\'image ne doit pas dépasser 5MB';
        return;
      }

      this.selectedFile = file;
      
      // Prévisualisation
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.thumbnailPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  removeThumbnail() {
    this.selectedFile = null;
    this.thumbnailPreview = null;
    this.cours.thumbnailUrl = undefined;
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.success = '';

    // Si un fichier est sélectionné, l'uploader d'abord
    if (this.selectedFile) {
      this.uploadingThumbnail = true;
      this.coursService.uploadThumbnail(this.selectedFile).subscribe({
        next: (response) => {
          console.log('Upload response:', response);
          this.cours.thumbnailUrl = response.filename;
          this.uploadingThumbnail = false;
          this.saveCours();
        },
        error: (err) => {
          console.error('Upload error:', err);
          this.error = err.error?.message || 'Erreur lors de l\'upload de l\'image';
          this.loading = false;
          this.uploadingThumbnail = false;
        }
      });
    } else {
      this.saveCours();
    }
  }

  private saveCours() {
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
