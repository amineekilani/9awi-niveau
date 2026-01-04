import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { CoursService, Cours } from '../cours.service';
import { ParcoursService, ParcoursResponse, ParcoursEtapeRequest, ParcoursEtapeResponse, NiveauDifficulte } from '../parcours.service';

declare const feather: any;

interface CoursAvailable extends Cours {
  isInParcours?: boolean;
}

@Component({
  selector: 'app-parcours-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-manager.component.html',
  styleUrls: ['./parcours-manager.component.css']
})
export class ParcoursManagerComponent implements OnInit {
  parcoursId!: number;
  parcours?: ParcoursResponse;
  etapes: ParcoursEtapeResponse[] = [];
  coursDisponibles: CoursAvailable[] = [];
  
  loading = false;
  error = '';
  success = '';
  
  // Modes d'affichage
  activeTab: 'overview' | 'etapes' | 'statistiques' = 'etapes';
  
  // Formulaire d'ajout d'étape
  etapeForm: FormGroup;
  showAddEtapeForm = false;
  editingEtape?: ParcoursEtapeResponse;

  // Niveaux d'étapes
  niveauxEtape = [
    { value: 1, label: 'Fondamental', color: 'bg-green-100 text-green-800' },
    { value: 2, label: 'Intermédiaire', color: 'bg-yellow-100 text-yellow-800' },
    { value: 3, label: 'Avancé', color: 'bg-red-100 text-red-800' }
  ];

  constructor(
    private fb: FormBuilder,
    private parcoursService: ParcoursService,
    private coursService: CoursService,
    private authService: AuthService,
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
      this.loadCoursDisponibles();
    });

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
      niveauEtape: [1, Validators.required],
      isObligatoire: [true],
      scoreMinimum: [0, [Validators.min(0), Validators.max(100)]],
      pourcentageCompletionRequis: [100, [Validators.min(0), Validators.max(100)]],
      quizObligatoires: [false],
      description: ['']
    });
  }

  loadParcours() {
    this.loading = true;
    this.parcoursService.getParcoursById(this.parcoursId).subscribe({
      next: (parcours) => {
        this.parcours = parcours;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du parcours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadEtapes() {
    this.parcoursService.getEtapesByParcours(this.parcoursId).subscribe({
      next: (etapes) => {
        this.etapes = etapes.sort((a, b) => a.ordreEtape - b.ordreEtape);
        this.updateCoursDisponibles();
        
        // Réinitialiser Feather icons
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des étapes';
        console.error('Erreur:', err);
      }
    });
  }

  loadCoursDisponibles() {
    this.coursService.getMesCours().subscribe({
      next: (cours) => {
        this.coursDisponibles = cours.filter(c => !c.archived);
        this.updateCoursDisponibles();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des cours:', err);
      }
    });
  }

  updateCoursDisponibles() {
    const coursInParcours = new Set(this.etapes.map(e => e.coursId));
    this.coursDisponibles.forEach(cours => {
      cours.isInParcours = coursInParcours.has(cours.id!);
    });
  }

  switchTab(tab: 'overview' | 'etapes' | 'statistiques') {
    this.activeTab = tab;
  }

  // Réorganisation des étapes (version simplifiée)
  moveEtapeUp(index: number) {
    if (index > 0) {
      const temp = this.etapes[index];
      this.etapes[index] = this.etapes[index - 1];
      this.etapes[index - 1] = temp;
      
      // Mettre à jour les ordres
      this.etapes.forEach((etape, i) => {
        etape.ordreEtape = i + 1;
      });
      
      this.saveEtapesOrder();
    }
  }

  moveEtapeDown(index: number) {
    if (index < this.etapes.length - 1) {
      const temp = this.etapes[index];
      this.etapes[index] = this.etapes[index + 1];
      this.etapes[index + 1] = temp;
      
      // Mettre à jour les ordres
      this.etapes.forEach((etape, i) => {
        etape.ordreEtape = i + 1;
      });
      
      this.saveEtapesOrder();
    }
  }

  saveEtapesOrder() {
    const nouvelOrdre = this.etapes.map(e => e.id);
    this.parcoursService.reorderEtapes(this.parcoursId, nouvelOrdre).subscribe({
      next: (etapesUpdated) => {
        this.etapes = etapesUpdated;
        this.success = 'Ordre des étapes mis à jour';
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = 'Erreur lors de la réorganisation';
        this.loadEtapes(); // Recharger en cas d'erreur
        console.error('Erreur:', err);
      }
    });
  }

  // Gestion des étapes
  showAddEtape() {
    this.showAddEtapeForm = true;
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
    this.showAddEtapeForm = true;
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

  cancelEtapeForm() {
    this.showAddEtapeForm = false;
    this.editingEtape = undefined;
    this.etapeForm.reset();
  }

  saveEtape() {
    if (this.etapeForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    const etapeData = this.etapeForm.value as ParcoursEtapeRequest;

    const request = this.editingEtape
      ? this.parcoursService.updateEtape(this.editingEtape.id, etapeData)
      : this.parcoursService.addEtapeToParcours(this.parcoursId, etapeData);

    request.subscribe({
      next: () => {
        this.success = this.editingEtape ? 'Étape modifiée avec succès' : 'Étape ajoutée avec succès';
        this.cancelEtapeForm();
        this.loadEtapes();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = 'Erreur lors de la sauvegarde de l\'étape';
        console.error('Erreur:', err);
      }
    });
  }

  deleteEtape(etape: ParcoursEtapeResponse) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'étape "${etape.coursTitle}" ?`)) {
      return;
    }

    this.parcoursService.deleteEtape(etape.id).subscribe({
      next: (response) => {
        console.log('Suppression réussie:', response);
        this.success = 'Étape supprimée avec succès';
        this.loadEtapes();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        console.error('Erreur complète:', err);
        console.error('Status:', err.status);
        console.error('StatusText:', err.statusText);
        console.error('Error body:', err.error);
        console.error('Message:', err.message);
        
        // Extraire le message d'erreur de manière plus robuste
        let errorMessage = 'Erreur lors de la suppression de l\'étape';
        
        if (err.error) {
          if (typeof err.error === 'string') {
            errorMessage += ': ' + err.error;
          } else if (err.error.error) {
            errorMessage += ': ' + err.error.error;
          } else if (err.error.message) {
            errorMessage += ': ' + err.error.message;
          } else {
            errorMessage += ': ' + JSON.stringify(err.error);
          }
        } else if (err.message) {
          errorMessage += ': ' + err.message;
        }
        
        this.error = errorMessage;
      }
    });
  }

  // Utilitaires
  markFormGroupTouched() {
    Object.keys(this.etapeForm.controls).forEach(key => {
      const control = this.etapeForm.get(key);
      control?.markAsTouched();
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.etapeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.etapeForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return 'Ce champ est obligatoire';
      if (field.errors['min']) return `La valeur doit être au minimum ${field.errors['min'].min}`;
      if (field.errors['max']) return `La valeur ne peut pas dépasser ${field.errors['max'].max}`;
    }
    return '';
  }

  getNiveauEtapeInfo(niveau: number) {
    return this.niveauxEtape.find(n => n.value === niveau) || this.niveauxEtape[0];
  }

  getNiveauDisplayName(niveau: NiveauDifficulte): string {
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getCoursById(coursId: number): CoursAvailable | undefined {
    return this.coursDisponibles.find(c => c.id === coursId);
  }

  clearMessages() {
    this.error = '';
    this.success = '';
  }

  goBack() {
    this.router.navigate(['/parcours-dashboard']);
  }

  editParcoursInfo() {
    this.router.navigate(['/parcours/modifier', this.parcoursId]);
  }
}