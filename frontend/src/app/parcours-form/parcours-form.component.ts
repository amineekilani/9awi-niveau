import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursRequest, ParcoursResponse, NiveauDifficulte, TypeParcours } from '../parcours.service';
import { CoursService, Cours } from '../cours.service';
import { DOMAINES_SPECIALISATION } from '../constants/domaines';

declare const feather: any;

@Component({
  selector: 'app-parcours-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-form.component.html',
  styleUrls: ['./parcours-form.component.css']
})
export class ParcoursFormComponent implements OnInit {
  parcoursForm: FormGroup;
  loading = false;
  error = '';
  success = '';
  isEditMode = false;
  parcoursId?: number;

  // Options pour les sélecteurs
  categories = DOMAINES_SPECIALISATION;
  niveaux = Object.values(NiveauDifficulte);
  typesParcours = Object.values(TypeParcours);

  // Cours disponibles pour les étapes
  coursDisponibles: Cours[] = [];
  loadingCours = false;

  // Upload d'image
  selectedFile?: File;
  imagePreview?: string;
  uploadingImage = false;

  constructor(
    private fb: FormBuilder,
    private parcoursService: ParcoursService,
    private coursService: CoursService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.parcoursForm = this.createForm();
  }

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    // Vérifier si c'est un mode édition
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.parcoursId = +params['id'];
        this.loadParcours();
      }
    });

    this.loadMesCours();

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  createForm(): FormGroup {
    return this.fb.group({
      titre: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(2000)]],
      thumbnailUrl: [''],
      categorie: [''],
      niveauDifficulte: [NiveauDifficulte.DEBUTANT],
      dureeEstimeeHeures: [null, [Validators.min(0)]],
      prerequis: ['', [Validators.maxLength(1000)]],
      typeParcours: [TypeParcours.LINEAIRE, Validators.required],
      pointsBonus: [0, [Validators.min(0)]],
      certificatEnabled: [false],
      isPublished: [false]
    });
  }

  loadParcours() {
    if (!this.parcoursId) return;

    this.loading = true;
    this.parcoursService.getParcoursById(this.parcoursId).subscribe({
      next: (parcours) => {
        this.parcoursForm.patchValue({
          titre: parcours.titre,
          description: parcours.description,
          thumbnailUrl: parcours.thumbnailUrl,
          categorie: parcours.categorie,
          niveauDifficulte: parcours.niveauDifficulte,
          dureeEstimeeHeures: parcours.dureeEstimeeHeures,
          prerequis: parcours.prerequis,
          typeParcours: parcours.typeParcours,
          pointsBonus: parcours.pointsBonus,
          certificatEnabled: parcours.certificatEnabled,
          isPublished: parcours.isPublished
        });

        if (parcours.thumbnailUrl) {
          this.imagePreview = this.parcoursService.getImageUrl(parcours.thumbnailUrl);
        }

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du parcours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadMesCours() {
    this.loadingCours = true;
    this.coursService.getMesCours().subscribe({
      next: (cours) => {
        this.coursDisponibles = cours;
        this.loadingCours = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des cours:', err);
        this.loadingCours = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Vérifications
      if (file.size > 5 * 1024 * 1024) { // 5MB
        this.error = 'Le fichier est trop volumineux (max 5MB)';
        return;
      }

      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (!allowedTypes.includes(file.type)) {
        this.error = 'Type de fichier non autorisé. Utilisez JPG, PNG, GIF ou WebP';
        return;
      }

      this.selectedFile = file;

      // Prévisualisation
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  async uploadImage(): Promise<string | null> {
    if (!this.selectedFile) return null;

    this.uploadingImage = true;
    try {
      const response = await this.parcoursService.uploadParcoursImage(this.selectedFile).toPromise();
      this.uploadingImage = false;
      return response?.filename || null;
    } catch (error) {
      this.uploadingImage = false;
      console.error('Erreur upload:', error);
      throw error;
    }
  }

  async onSubmit() {
    if (this.parcoursForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    try {
      // Upload de l'image si nécessaire
      let thumbnailUrl = this.parcoursForm.get('thumbnailUrl')?.value;
      if (this.selectedFile) {
        thumbnailUrl = await this.uploadImage();
      }

      const parcoursData: ParcoursRequest = {
        ...this.parcoursForm.value,
        thumbnailUrl: thumbnailUrl
      };

      if (this.isEditMode && this.parcoursId) {
        // Mode édition
        this.parcoursService.updateParcours(this.parcoursId, parcoursData).subscribe({
          next: (response) => {
            this.success = 'Parcours mis à jour avec succès';
            this.loading = false;
            setTimeout(() => {
              this.router.navigate(['/parcours/gerer', response.id]);
            }, 1500);
          },
          error: (err) => {
            this.error = this.extractErrorMessage(err);
            this.loading = false;
          }
        });
      } else {
        // Mode création
        this.parcoursService.createParcours(parcoursData).subscribe({
          next: (response) => {
            this.success = 'Parcours créé avec succès';
            this.loading = false;
            setTimeout(() => {
              this.router.navigate(['/parcours/gerer', response.id]);
            }, 1500);
          },
          error: (err) => {
            this.error = this.extractErrorMessage(err);
            this.loading = false;
          }
        });
      }
    } catch (error) {
      this.error = 'Erreur lors de l\'upload de l\'image';
      this.loading = false;
    }
  }

  onCancel() {
    if (this.isEditMode && this.parcoursId) {
      this.router.navigate(['/parcours/gerer', this.parcoursId]);
    } else {
      this.router.navigate(['/parcours-dashboard']);
    }
  }

  // Utilitaires de validation
  isFieldInvalid(fieldName: string): boolean {
    const field = this.parcoursForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.parcoursForm.get(fieldName);
    if (field && field.errors) {
      if (field.errors['required']) return `${this.getFieldLabel(fieldName)} est obligatoire`;
      if (field.errors['maxlength']) return `${this.getFieldLabel(fieldName)} est trop long`;
      if (field.errors['min']) return `${this.getFieldLabel(fieldName)} doit être positif`;
    }
    return '';
  }

  getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      'titre': 'Le titre',
      'description': 'La description',
      'categorie': 'La catégorie',
      'dureeEstimeeHeures': 'La durée estimée',
      'prerequis': 'Les prérequis',
      'pointsBonus': 'Les points bonus'
    };
    return labels[fieldName] || fieldName;
  }

  markFormGroupTouched() {
    Object.keys(this.parcoursForm.controls).forEach(key => {
      const control = this.parcoursForm.get(key);
      control?.markAsTouched();
    });
  }

  extractErrorMessage(error: any): string {
    if (error.error && typeof error.error === 'string') {
      return error.error;
    }
    if (error.message) {
      return error.message;
    }
    return 'Une erreur est survenue';
  }

  // Utilitaires d'affichage
  getNiveauDisplayName(niveau: NiveauDifficulte): string {
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getTypeParcoursDisplayName(type: TypeParcours): string {
    return this.parcoursService.getTypeParcoursDisplayName(type);
  }

  clearMessages() {
    this.error = '';
    this.success = '';
  }

  removeImage() {
    this.selectedFile = undefined;
    this.imagePreview = undefined;
    this.parcoursForm.patchValue({ thumbnailUrl: '' });
  }
}