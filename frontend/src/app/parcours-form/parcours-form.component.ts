import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursRequest, ParcoursResponse, NiveauDifficulte, TypeParcours } from '../parcours.service';
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
  niveauxDifficulte = Object.values(NiveauDifficulte);
  typesParcours = Object.values(TypeParcours);
  categories = [...DOMAINES_SPECIALISATION];

  // Upload d'image
  selectedFile?: File;
  imagePreview?: string;
  uploadProgress = 0;

  constructor(
    private fb: FormBuilder,
    private parcoursService: ParcoursService,
    private authService: AuthService,
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

    // Vérifier si on est en mode édition
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.parcoursId = +params['id'];
        this.loadParcours();
      } else {
        // Pré-remplir la catégorie avec le domaine du formateur (seulement en mode création)
        const domaineFormateur = this.authService.getDomaine();
        console.log('Domaine du formateur:', domaineFormateur); // Debug
        if (domaineFormateur) {
          // Vérifier si le domaine du formateur existe dans nos catégories
          const categorieCorrespondante = this.categories.includes(domaineFormateur) 
            ? domaineFormateur 
            : 'Autre'; // Fallback vers "Autre" si le domaine n'existe pas
          
          this.parcoursForm.patchValue({
            categorie: categorieCorrespondante
          });
          console.log('Catégorie pré-remplie avec:', categorieCorrespondante); // Debug
          
          if (categorieCorrespondante === 'Autre') {
            console.log('Domaine non reconnu, fallback vers "Autre"');
          }
        }
      }
    });

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  createForm(): FormGroup {
    return this.fb.group({
      titre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(5000)]],
      categorie: [''],
      niveauDifficulte: [NiveauDifficulte.DEBUTANT],
      dureeEstimeeHeures: [null, [Validators.min(1), Validators.max(1000)]],
      prerequis: ['', [Validators.maxLength(2000)]],
      typeParcours: [TypeParcours.LINEAIRE, Validators.required],
      pointsBonus: [0, [Validators.min(0), Validators.max(10000)]],
      badgeCompletion: [''],
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
          categorie: parcours.categorie,
          niveauDifficulte: parcours.niveauDifficulte,
          dureeEstimeeHeures: parcours.dureeEstimeeHeures,
          prerequis: parcours.prerequis,
          typeParcours: parcours.typeParcours,
          pointsBonus: parcours.pointsBonus,
          badgeCompletion: parcours.badgeCompletion,
          certificatEnabled: parcours.certificatEnabled,
          isPublished: parcours.isPublished
        });

        // Charger l'image si elle existe
        if (parcours.thumbnailUrl) {
          this.imagePreview = `http://localhost:8080/images/parcours/${parcours.thumbnailUrl}`;
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

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Vérifier le type de fichier
      if (!file.type.startsWith('image/')) {
        this.error = 'Veuillez sélectionner un fichier image valide';
        return;
      }

      // Vérifier la taille (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'L\'image ne doit pas dépasser 5MB';
        return;
      }

      this.selectedFile = file;

      // Créer un aperçu
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadImage(): Promise<string | null> {
    if (!this.selectedFile) {
      return Promise.resolve(null);
    }

    return new Promise((resolve, reject) => {
      this.parcoursService.uploadParcoursImage(this.selectedFile!).subscribe({
        next: (response) => {
          console.log('Upload réussi:', response.filename);
          resolve(response.filename);
        },
        error: (err) => {
          console.error('Erreur upload:', err);
          this.error = 'Erreur lors de l\'upload de l\'image';
          reject(err);
        }
      });
    });
  }

  removeImage() {
    this.selectedFile = undefined;
    this.imagePreview = undefined;
    // Reset du champ file input
    const fileInput = document.getElementById('thumbnail') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  async onSubmit() {
    if (this.parcoursForm.invalid) {
      this.markFormGroupTouched();
      this.error = 'Veuillez corriger les erreurs dans le formulaire';
      return;
    }

    this.loading = true;
    this.error = '';

    try {
      // Upload de l'image d'abord si nécessaire
      let thumbnailUrl = null;
      if (this.selectedFile) {
        thumbnailUrl = await this.uploadImage();
      }

      const formData = this.parcoursForm.value as ParcoursRequest;
      
      // Ajouter l'URL de l'image si uploadée
      if (thumbnailUrl) {
        formData.thumbnailUrl = thumbnailUrl;
      }

      const request = this.isEditMode 
        ? this.parcoursService.updateParcours(this.parcoursId!, formData)
        : this.parcoursService.createParcours(formData);

      request.subscribe({
        next: (response) => {
          this.success = this.isEditMode 
            ? 'Parcours modifié avec succès' 
            : 'Parcours créé avec succès';
          
          setTimeout(() => {
            this.router.navigate(['/parcours/gerer', response.id]);
          }, 1500);
        },
        error: (err) => {
          this.error = this.isEditMode 
            ? 'Erreur lors de la modification du parcours' 
            : 'Erreur lors de la création du parcours';
          this.loading = false;
          console.error('Erreur:', err);
        }
      });
    } catch (uploadError) {
      this.loading = false;
      // L'erreur d'upload est déjà gérée dans uploadImage()
    }
  }

  markFormGroupTouched() {
    Object.keys(this.parcoursForm.controls).forEach(key => {
      const control = this.parcoursForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.parcoursForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.parcoursForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${this.getFieldLabel(fieldName)} est obligatoire`;
      if (field.errors['minlength']) return `${this.getFieldLabel(fieldName)} doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
      if (field.errors['maxlength']) return `${this.getFieldLabel(fieldName)} ne peut pas dépasser ${field.errors['maxlength'].requiredLength} caractères`;
      if (field.errors['min']) return `La valeur doit être au minimum ${field.errors['min'].min}`;
      if (field.errors['max']) return `La valeur ne peut pas dépasser ${field.errors['max'].max}`;
    }
    return '';
  }

  getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      titre: 'Le titre',
      description: 'La description',
      categorie: 'La catégorie',
      dureeEstimeeHeures: 'La durée estimée',
      prerequis: 'Les prérequis',
      pointsBonus: 'Les points bonus',
      badgeCompletion: 'Le badge de completion'
    };
    return labels[fieldName] || fieldName;
  }

  getNiveauDisplayName(niveau: NiveauDifficulte): string {
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getTypeParcoursDisplayName(type: TypeParcours): string {
    return this.parcoursService.getTypeParcoursDisplayName(type);
  }

  cancel() {
    if (this.isEditMode && this.parcoursId) {
      this.router.navigate(['/parcours/gerer', this.parcoursId]);
    } else {
      this.router.navigate(['/parcours-dashboard']);
    }
  }

  clearMessages() {
    this.error = '';
    this.success = '';
  }
}