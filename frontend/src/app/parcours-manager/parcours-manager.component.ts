import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, NiveauDifficulte, TypeParcours } from '../parcours.service';

declare const feather: any;

@Component({
  selector: 'app-parcours-manager',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-manager.component.html',
  styleUrls: ['./parcours-manager.component.css']
})
export class ParcoursManagerComponent implements OnInit {
  parcours?: ParcoursResponse;
  loading = false;
  error = '';
  success = '';
  
  parcoursId!: number;
  Math = Math; // Exposer Math pour le template

  constructor(
    private parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    this.route.params.subscribe(params => {
      this.parcoursId = +params['id'];
      this.loadParcours();
    });

    // Initialiser Feather icons
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
      next: (parcours) => {
        this.parcours = parcours;
        this.loading = false;
        
        // Réinitialiser les icônes
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du parcours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  togglePublish() {
    if (!this.parcours) return;

    // Vérifier que le parcours a au moins une étape avant publication
    if (!this.parcours.isPublished && this.parcours.nombreEtapes === 0) {
      this.error = 'Un parcours doit avoir au moins une étape pour être publié';
      return;
    }

    const action = this.parcours.isPublished ? 'dépublier' : 'publier';
    if (!confirm(`Êtes-vous sûr de vouloir ${action} ce parcours ?`)) {
      return;
    }

    this.parcoursService.togglePublishParcours(this.parcours.id).subscribe({
      next: (updatedParcours) => {
        this.parcours = updatedParcours;
        this.success = `Parcours ${action === 'publier' ? 'publié' : 'dépublié'} avec succès`;
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
      }
    });
  }

  deleteParcours() {
    if (!this.parcours) return;

    if (!confirm(`Êtes-vous sûr de vouloir supprimer le parcours "${this.parcours.titre}" ? Cette action est irréversible.`)) {
      return;
    }

    this.parcoursService.deleteParcours(this.parcours.id).subscribe({
      next: () => {
        this.success = 'Parcours supprimé avec succès';
        setTimeout(() => {
          this.router.navigate(['/parcours-dashboard']);
        }, 1500);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
      }
    });
  }

  // Navigation
  editParcours() {
    this.router.navigate(['/parcours/modifier', this.parcoursId]);
  }

  manageEtapes() {
    this.router.navigate(['/parcours', this.parcoursId, 'etapes']);
  }

  viewParcours() {
    this.router.navigate(['/parcours', this.parcoursId]);
  }

  viewStatistics() {
    // TODO: Implémenter la vue des statistiques détaillées
    this.error = 'Statistiques détaillées à venir';
    setTimeout(() => this.error = '', 3000);
  }

  // Utilitaires d'affichage
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
      case NiveauDifficulte.EXPERT:
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getTypeParcoursDisplayName(type: TypeParcours): string {
    return this.parcoursService.getTypeParcoursDisplayName(type);
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

  getStatusBadgeClass(): string {
    if (!this.parcours) return 'bg-gray-100 text-gray-800';
    
    if (this.parcours.isPublished) {
      return 'bg-green-100 text-green-800';
    }
    return 'bg-gray-100 text-gray-800';
  }

  getStatusText(): string {
    if (!this.parcours) return 'Inconnu';
    return this.parcours.isPublished ? 'Publié' : 'Brouillon';
  }

  getTauxCompletion(): number {
    if (!this.parcours || this.parcours.nombreInscriptions === 0) return 0;
    return Math.round((this.parcours.nombreCompletions / this.parcours.nombreInscriptions) * 100);
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

  getImageUrl(thumbnailUrl: string | undefined): string {
    if (!thumbnailUrl) return '/assets/images/default-parcours.jpg';
    return this.parcoursService.getImageUrl(thumbnailUrl);
  }
}