import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, ParcoursEtapeResponse, NiveauDifficulte } from '../parcours.service';
import { ParcoursValidationService, EtapeValidation } from '../parcours-validation.service';

declare const feather: any;

@Component({
  selector: 'app-parcours-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-detail.component.html',
  styleUrls: ['./parcours-detail.component.css']
})
export class ParcoursDetailComponent implements OnInit {
  parcours: ParcoursResponse | null = null;
  etapes: ParcoursEtapeResponse[] = [];
  etapesValidation: EtapeValidation[] = [];
  loading = false;
  error = '';
  parcoursId!: number;

  // États d'inscription
  isInscrit = false;
  inscriptionLoading = false;

  constructor(
    public parcoursService: ParcoursService,
    private validationService: ParcoursValidationService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.parcoursId = +params['id'];
      this.loadParcours();
      this.loadEtapes();
    });
    
    // Initialiser Feather icons après le rendu
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  loadParcours() {
    this.loading = true;
    this.error = '';

    this.parcoursService.getParcoursById(this.parcoursId).subscribe({
      next: (data) => {
        this.parcours = data;
        this.isInscrit = data.isInscrit || false;
        this.loading = false;
        
        // Réinitialiser les icônes après le chargement
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (error) => {
        console.error('Erreur lors du chargement du parcours:', error);
        this.error = 'Erreur lors du chargement du parcours';
        this.loading = false;
      }
    });
  }

  loadEtapes() {
    this.parcoursService.getEtapesByParcours(this.parcoursId).subscribe({
      next: (data) => {
        this.etapes = data.sort((a, b) => a.ordreEtape - b.ordreEtape);
        
        // Valider les étapes si l'utilisateur est inscrit
        if (this.parcours && this.parcours.isInscrit) {
          this.validerEtapes();
        }
        
        // Réinitialiser les icônes après le chargement
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des étapes:', error);
      }
    });
  }

  validerEtapes() {
    if (!this.parcours || !this.etapes.length) return;

    this.validationService.validerEtapesParcours(this.parcours, this.etapes).subscribe({
      next: (validations) => {
        this.etapesValidation = validations;
        
        // Mettre à jour les étapes avec les informations de validation
        this.etapes.forEach((etape, index) => {
          const validation = validations.find(v => v.etapeId === etape.id);
          if (validation) {
            etape.isDebloque = validation.isDebloque;
            etape.isComplete = validation.isComplete;
            etape.progressionCours = validation.progressionCours;
            etape.scoreObtenu = validation.scoreObtenu;
          }
        });
        
        // Réinitialiser les icônes
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (error) => {
        console.error('Erreur lors de la validation des étapes:', error);
      }
    });
  }

  sInscrireAuParcours() {
    if (this.inscriptionLoading) return;
    
    this.inscriptionLoading = true;
    
    this.parcoursService.sInscrireAuParcours(this.parcoursId).subscribe({
      next: (response) => {
        this.isInscrit = true;
        this.inscriptionLoading = false;
        
        if (this.parcours) {
          this.parcours.isInscrit = true;
          this.parcours.nombreInscriptions++;
          this.parcours.progressionUtilisateur = 0;
          this.parcours.etapeCouranteUtilisateur = 1;
        }
        
        // Recharger les données pour avoir les informations à jour
        this.loadParcours();
      },
      error: (error) => {
        console.error('Erreur lors de l\'inscription:', error);
        this.error = 'Erreur lors de l\'inscription au parcours';
        this.inscriptionLoading = false;
      }
    });
  }

  commencerParcours() {
    if (!this.etapes.length) return;
    
    // Rediriger vers le premier cours du parcours
    const premiereEtape = this.etapes[0];
    this.router.navigate(['/cours', premiereEtape.coursId]);
  }

  continuerParcours() {
    if (!this.parcours || !this.etapes.length) return;
    
    // Trouver l'étape courante ou la première étape non terminée
    const etapeCourante = this.parcours.etapeCouranteUtilisateur || 1;
    const etape = this.etapes.find(e => e.ordreEtape === etapeCourante) || this.etapes[0];
    
    this.router.navigate(['/cours', etape.coursId]);
  }

  voirCours(etape: ParcoursEtapeResponse) {
    // Vérifier si l'étape est déverrouillée
    if (this.isEtapeVerrouillee(etape)) {
      return; // Ne rien faire si l'étape est verrouillée
    }
    
    this.router.navigate(['/cours', etape.coursId]);
  }

  retourCatalogue() {
    this.router.navigate(['/parcours']);
  }

  // Utilitaires pour l'affichage
  getNiveauDisplayName(niveau: NiveauDifficulte | undefined): string {
    if (!niveau) return 'Non défini';
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getNiveauColor(niveau: NiveauDifficulte | undefined): string {
    if (!niveau) return 'bg-gray-100 text-gray-800';
    
    switch (niveau) {
      case NiveauDifficulte.DEBUTANT:
        return 'bg-green-100 text-green-800';
      case NiveauDifficulte.INTERMEDIAIRE:
        return 'bg-yellow-100 text-yellow-800';
      case NiveauDifficulte.AVANCE:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDuree(heures: number | undefined): string {
    if (!heures) return 'Non définie';
    
    if (heures < 1) {
      return `${Math.round(heures * 60)} min`;
    } else if (heures === 1) {
      return '1 heure';
    } else {
      return `${heures} heures`;
    }
  }

  getNiveauEtapeDisplayName(niveau: number): string {
    return this.parcoursService.getNiveauEtapeDisplayName(niveau);
  }

  getNiveauEtapeColor(niveau: number): string {
    switch (niveau) {
      case 1:
        return 'bg-blue-100 text-blue-800';
      case 2:
        return 'bg-purple-100 text-purple-800';
      case 3:
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getProgressionColor(progression: number): string {
    if (progression >= 80) return 'bg-green-500';
    if (progression >= 50) return 'bg-yellow-500';
    return 'bg-blue-500';
  }

  // Logique de verrouillage des étapes (NOUVELLE VERSION AVEC VALIDATIONS)
  isEtapeVerrouillee(etape: ParcoursEtapeResponse): boolean {
    if (!this.isInscrit) return true;
    
    // Utiliser les validations calculées si disponibles
    const validation = this.etapesValidation.find(v => v.etapeId === etape.id);
    if (validation) {
      return !validation.isDebloque;
    }
    
    // Fallback vers l'ancienne logique si pas de validation
    return etape.isDebloque === false;
  }

  getStatutEtape(etape: ParcoursEtapeResponse): 'locked' | 'available' | 'current' | 'completed' {
    if (!this.isInscrit) return 'locked';
    
    // Utiliser les validations calculées si disponibles
    const validation = this.etapesValidation.find(v => v.etapeId === etape.id);
    if (validation) {
      if (validation.isComplete) return 'completed';
      if (validation.isDebloque) return 'available';
      return 'locked';
    }
    
    // Fallback vers l'ancienne logique
    if (etape.isComplete) return 'completed';
    if (etape.isDebloque) return 'available';
    return 'locked';
  }

  getRaisonVerrouillage(etape: ParcoursEtapeResponse): string {
    const validation = this.etapesValidation.find(v => v.etapeId === etape.id);
    return validation?.raisonVerrouillage || '';
  }

  getConditionsManquantes(etape: ParcoursEtapeResponse): string[] {
    const validation = this.etapesValidation.find(v => v.etapeId === etape.id);
    if (!validation) return [];

    const conditions = [];
    
    if (!validation.conditionsRemplies.scoreMinimum && etape.scoreMinimum > 0) {
      conditions.push(`Score minimum: ${etape.scoreMinimum}% (obtenu: ${validation.scoreObtenu}%)`);
    }
    
    if (!validation.conditionsRemplies.pourcentageCompletion && etape.pourcentageCompletionRequis > 0) {
      conditions.push(`Completion: ${etape.pourcentageCompletionRequis}% (actuel: ${validation.progressionCours}%)`);
    }
    
    if (!validation.conditionsRemplies.quizObligatoires && etape.quizObligatoires) {
      conditions.push('Quiz obligatoires non réussis');
    }

    return conditions;
  }

  getIconeStatut(statut: string): string {
    switch (statut) {
      case 'completed':
        return 'check-circle';
      case 'current':
        return 'play-circle';
      case 'available':
        return 'circle';
      case 'locked':
      default:
        return 'lock';
    }
  }

  forcerMiseAJourProgression() {
    if (!this.parcours) return;
    
    this.parcoursService.forcerMiseAJourProgression(this.parcours.id).subscribe({
      next: (response) => {
        console.log('✅ Progression mise à jour:', response.message);
        // Recharger les données pour voir les changements
        this.loadParcours();
        this.loadEtapes();
      },
      error: (error) => {
        console.error('❌ Erreur lors de la mise à jour:', error);
      }
    });
  }

  recalculerProgression() {
    this.parcoursService.recalculerProgression().subscribe({
      next: (response) => {
        console.log('Progression recalculée:', response.message);
        // Recharger les données
        this.loadParcours();
        this.loadEtapes();
      },
      error: (error) => {
        console.error('Erreur lors du recalcul de la progression:', error);
      }
    });
  }

  getCouleurStatut(statut: string): string {
    switch (statut) {
      case 'completed':
        return 'text-green-600';
      case 'current':
        return 'text-blue-600';
      case 'available':
        return 'text-gray-600';
      case 'locked':
      default:
        return 'text-gray-400';
    }
  }
}