import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, ParcoursEtapeRequest, ParcoursEtapeResponse } from '../parcours.service';
import { CoursService, Cours } from '../cours.service';

declare const feather: any;

@Component({
  selector: 'app-parcours-etapes',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-etapes.component.html',
  styleUrls: ['./parcours-etapes.component.css']
})
export class ParcoursEtapesComponent implements OnInit {
  parcours?: ParcoursResponse;
  etapes: ParcoursEtapeResponse[] = [];
  coursDisponibles: Cours[] = [];
  
  loading = false;
  error = '';
  success = '';
  
  // Formulaire d'ajout d'étape
  etapeForm: FormGroup;
  showAddForm = false;
  editingEtape?: ParcoursEtapeResponse;
  
  // Drag & Drop
  draggedEtape?: ParcoursEtapeResponse;
  
  parcoursId!: number;

  constructor(
    private fb: FormBuilder,
    private parcoursService: ParcoursService,
    private coursService: CoursService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.etapeForm = this.createEtapeForm();
  }

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    this.route.params.subscribe(params => {
      this.parcoursId = +params['id'];
      this.loadParcours();
      this.loadEtapes();
    });

    this.loadMesCours();

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  createEtapeForm(): FormGroup {
    return this.fb.group({
      coursId: [null, Validators.required],
      ordreEtape: [1, [Validators.required, Validators.min(1)]],
      niveauEtape: [1, [Validators.required, Validators.min(1), Validators.max(3)]],
      isObligatoire: [true],
      scoreMinimum: [0, [Validators.min(0), Validators.max(100)]],
      pourcentageCompletionRequis: [100, [Validators.required, Validators.min(0), Validators.max(100)]],
      quizObligatoires: [false],
      description: ['']
    });
  }

  loadParcours() {
    this.parcoursService.getParcoursById(this.parcoursId).subscribe({
      next: (parcours) => {
        this.parcours = parcours;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du parcours';
        console.error('Erreur:', err);
      }
    });
  }

  loadEtapes() {
    this.loading = true;
    this.parcoursService.getEtapesByParcours(this.parcoursId).subscribe({
      next: (etapes) => {
        this.etapes = etapes.sort((a, b) => a.ordreEtape - b.ordreEtape);
        this.loading = false;
        
        // Réinitialiser les icônes
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des étapes';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadMesCours() {
    this.coursService.getMesCours().subscribe({
      next: (cours) => {
        this.coursDisponibles = cours;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des cours:', err);
      }
    });
  }

  showAddEtapeForm() {
    this.showAddForm = true;
    this.editingEtape = undefined;
    this.etapeForm.reset({
      ordreEtape: this.etapes.length + 1,
      niveauEtape: 1,
      isObligatoire: true,
      scoreMinimum: 0,
      pourcentageCompletionRequis: 100,
      quizObligatoires: false
    });
  }

  editEtape(etape: ParcoursEtapeResponse) {
    this.showAddForm = true;
    this.editingEtape = etape;
    this.etapeForm.patchValue({
      coursId: etape.coursId,
      ordreEtape: etape.ordreEtape,
      niveauEtape: etape.niveauEtape,
      isObligatoire: etape.isObligatoire,
      scoreMinimum: etape.scoreMinimum,
      pourcentageCompletionRequis: etape.pourcentageCompletionRequis,
      quizObligatoires: etape.quizObligatoires,
      description: etape.description
    });
  }

  cancelForm() {
    this.showAddForm = false;
    this.editingEtape = undefined;
    this.etapeForm.reset();
  }

  onSubmitEtape() {
    if (this.etapeForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    const etapeData: ParcoursEtapeRequest = this.etapeForm.value;

    if (this.editingEtape) {
      // Mode édition
      this.parcoursService.updateEtape(this.editingEtape.id, etapeData).subscribe({
        next: () => {
          this.success = 'Étape mise à jour avec succès';
          this.cancelForm();
          this.loadEtapes();
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = this.extractErrorMessage(err);
        }
      });
    } else {
      // Mode ajout
      this.parcoursService.addEtapeToParcours(this.parcoursId, etapeData).subscribe({
        next: () => {
          this.success = 'Étape ajoutée avec succès';
          this.cancelForm();
          this.loadEtapes();
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = this.extractErrorMessage(err);
        }
      });
    }
  }

  deleteEtape(etape: ParcoursEtapeResponse) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'étape "${etape.coursTitle}" ?`)) {
      return;
    }

    this.parcoursService.deleteEtape(etape.id).subscribe({
      next: () => {
        this.success = 'Étape supprimée avec succès';
        this.loadEtapes();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
      }
    });
  }

  // Drag & Drop
  onDragStart(event: DragEvent, etape: ParcoursEtapeResponse) {
    this.draggedEtape = etape;
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/html', etape.id.toString());
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  onDrop(event: DragEvent, targetEtape: ParcoursEtapeResponse) {
    event.preventDefault();
    
    if (!this.draggedEtape || this.draggedEtape.id === targetEtape.id) {
      return;
    }

    // Créer le nouvel ordre
    const newOrder = [...this.etapes];
    const draggedIndex = newOrder.findIndex(e => e.id === this.draggedEtape!.id);
    const targetIndex = newOrder.findIndex(e => e.id === targetEtape.id);

    // Déplacer l'élément
    const [removed] = newOrder.splice(draggedIndex, 1);
    newOrder.splice(targetIndex, 0, removed);

    // Mettre à jour les ordres
    const reorderedIds = newOrder.map(e => e.id);
    
    this.parcoursService.reorderEtapes(this.parcoursId, reorderedIds).subscribe({
      next: (etapes) => {
        this.etapes = etapes;
        this.success = 'Ordre des étapes mis à jour';
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
        this.loadEtapes(); // Recharger en cas d'erreur
      }
    });

    this.draggedEtape = undefined;
  }

  // Utilitaires
  getCoursById(coursId: number): Cours | undefined {
    return this.coursDisponibles.find(c => c.id === coursId);
  }

  getNiveauEtapeLabel(niveau: number): string {
    const labels: { [key: number]: string } = {
      1: 'Fondamental',
      2: 'Intermédiaire',
      3: 'Avancé'
    };
    return labels[niveau] || `Niveau ${niveau}`;
  }

  getNiveauEtapeColor(niveau: number): string {
    const colors: { [key: number]: string } = {
      1: 'bg-green-100 text-green-800',
      2: 'bg-yellow-100 text-yellow-800',
      3: 'bg-red-100 text-red-800'
    };
    return colors[niveau] || 'bg-gray-100 text-gray-800';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.etapeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.etapeForm.get(fieldName);
    if (field && field.errors) {
      if (field.errors['required']) return `Ce champ est obligatoire`;
      if (field.errors['min']) return `Valeur trop petite`;
      if (field.errors['max']) return `Valeur trop grande`;
    }
    return '';
  }

  markFormGroupTouched() {
    Object.keys(this.etapeForm.controls).forEach(key => {
      const control = this.etapeForm.get(key);
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

  clearMessages() {
    this.error = '';
    this.success = '';
  }

  trackByEtapeId(index: number, etape: ParcoursEtapeResponse): number {
    return etape.id;
  }
}