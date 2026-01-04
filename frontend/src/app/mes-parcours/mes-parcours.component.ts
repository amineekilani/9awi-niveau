import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, NiveauDifficulte, TypeParcours } from '../parcours.service';

declare const feather: any;

@Component({
  selector: 'app-mes-parcours',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './mes-parcours.component.html',
  styleUrls: ['./mes-parcours.component.css']
})
export class MesParcoursComponent implements OnInit {
  parcoursEnCours: ParcoursResponse[] = [];
  parcoursTermines: ParcoursResponse[] = [];
  loading = false;
  error = '';

  // Filtres et tri
  filterStatus = 'all'; // all, active, completed
  sortBy = 'recent'; // recent, progress, alphabetical

  // Statistiques
  totalParcours = 0;
  totalPointsGagnes = 0;
  moyenneProgression = 0;
  certificatsObtenus = 0;

  constructor(
    public parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Écouter les paramètres de requête pour le filtrage initial
    this.route.queryParams.subscribe(params => {
      if (params['filter']) {
        this.filterStatus = params['filter'];
      }
    });
    
    this.loadMesParcours();
    
    // Initialiser Feather icons après le rendu
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  loadMesParcours() {
    this.loading = true;
    this.error = '';

    this.parcoursService.getMesInscriptions().subscribe({
      next: (data) => {
        // Séparer les parcours en cours et terminés
        this.parcoursEnCours = data.filter(p => !p.isCompletedUtilisateur);
        this.parcoursTermines = data.filter(p => p.isCompletedUtilisateur);
        
        this.calculateStatistics();
        this.loading = false;
        
        // Réinitialiser les icônes après le chargement
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des parcours:', error);
        this.error = 'Erreur lors du chargement de vos parcours';
        this.loading = false;
      }
    });
  }

  calculateStatistics() {
    const allParcours = [...this.parcoursEnCours, ...this.parcoursTermines];
    
    this.totalParcours = allParcours.length;
    this.totalPointsGagnes = allParcours.reduce((sum, p) => sum + (p.pointsGagnesUtilisateur || 0), 0);
    this.moyenneProgression = allParcours.length > 0 
      ? Math.round(allParcours.reduce((sum, p) => sum + (p.progressionUtilisateur || 0), 0) / allParcours.length)
      : 0;
    this.certificatsObtenus = allParcours.filter(p => p.certificatGenere).length;
  }

  getFilteredParcours(): ParcoursResponse[] {
    let parcours: ParcoursResponse[] = [];
    
    switch (this.filterStatus) {
      case 'active':
        parcours = this.parcoursEnCours;
        break;
      case 'completed':
        parcours = this.parcoursTermines;
        break;
      case 'all':
      default:
        parcours = [...this.parcoursEnCours, ...this.parcoursTermines];
        break;
    }

    // Tri
    switch (this.sortBy) {
      case 'progress':
        parcours.sort((a, b) => (b.progressionUtilisateur || 0) - (a.progressionUtilisateur || 0));
        break;
      case 'alphabetical':
        parcours.sort((a, b) => a.titre.localeCompare(b.titre));
        break;
      case 'recent':
      default:
        parcours.sort((a, b) => new Date(b.dateInscription || '').getTime() - new Date(a.dateInscription || '').getTime());
        break;
    }

    return parcours;
  }

  onFilterChange() {
    // Les données sont déjà filtrées par getFilteredParcours()
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  continuerParcours(parcours: ParcoursResponse) {
    this.router.navigate(['/parcours', parcours.id]);
  }

  voirCertificat(parcours: ParcoursResponse) {
    if (parcours.certificatUrl) {
      window.open(parcours.certificatUrl, '_blank');
    }
  }

  explorerParcours() {
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

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getProgressionColor(progression: number): string {
    if (progression >= 80) return 'bg-green-500';
    if (progression >= 50) return 'bg-yellow-500';
    return 'bg-blue-500';
  }

  getStatusBadge(parcours: ParcoursResponse): { text: string, class: string } {
    if (parcours.isCompletedUtilisateur) {
      return { text: 'Terminé', class: 'bg-green-100 text-green-800' };
    } else if ((parcours.progressionUtilisateur || 0) > 0) {
      return { text: 'En cours', class: 'bg-blue-100 text-blue-800' };
    } else {
      return { text: 'Non commencé', class: 'bg-gray-100 text-gray-800' };
    }
  }

  getTempsRestantEstime(parcours: ParcoursResponse): string {
    if (parcours.isCompletedUtilisateur) return 'Terminé';
    
    const dureeHeures = parcours.dureeEstimeeHeures;
    if (!dureeHeures) return 'Non défini';
    
    const dureeRestante = dureeHeures * (100 - (parcours.progressionUtilisateur || 0)) / 100;
    return this.formatDuree(dureeRestante);
  }
}