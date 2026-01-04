import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, NiveauDifficulte, TypeParcours } from '../parcours.service';

declare const feather: any;

interface ParcoursStats {
  totalParcours: number;
  parcoursPublies: number;
  totalInscriptions: number;
  totalCompletions: number;
  tauxCompletionMoyen: number;
}

@Component({
  selector: 'app-parcours-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-dashboard.component.html',
  styleUrls: ['./parcours-dashboard.component.css']
})
export class ParcoursDashboardComponent implements OnInit {
  parcours: ParcoursResponse[] = [];
  loading = false;
  error = '';
  success = '';
  stats: ParcoursStats = {
    totalParcours: 0,
    parcoursPublies: 0,
    totalInscriptions: 0,
    totalCompletions: 0,
    tauxCompletionMoyen: 0
  };

  // Filtres et tri
  activeTab: 'tous' | 'publies' | 'brouillons' = 'tous';
  sortBy: 'recent' | 'titre' | 'inscriptions' = 'recent';

  constructor(
    private parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    this.loadParcours();
    
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

    this.parcoursService.getMesParcours().subscribe({
      next: (data) => {
        this.parcours = data;
        this.calculateStats();
        this.loading = false;
        
        // Réinitialiser Feather icons
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des parcours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  calculateStats() {
    this.stats.totalParcours = this.parcours.length;
    this.stats.parcoursPublies = this.parcours.filter(p => p.isPublished).length;
    this.stats.totalInscriptions = this.parcours.reduce((sum, p) => sum + p.nombreInscriptions, 0);
    this.stats.totalCompletions = this.parcours.reduce((sum, p) => sum + p.nombreCompletions, 0);
    
    if (this.stats.totalInscriptions > 0) {
      this.stats.tauxCompletionMoyen = Math.round((this.stats.totalCompletions / this.stats.totalInscriptions) * 100);
    }
  }

  get filteredParcours(): ParcoursResponse[] {
    let filtered = [...this.parcours];

    // Filtrer par onglet
    switch (this.activeTab) {
      case 'publies':
        filtered = filtered.filter(p => p.isPublished);
        break;
      case 'brouillons':
        filtered = filtered.filter(p => !p.isPublished);
        break;
    }

    // Trier
    switch (this.sortBy) {
      case 'titre':
        filtered.sort((a, b) => a.titre.localeCompare(b.titre));
        break;
      case 'inscriptions':
        filtered.sort((a, b) => b.nombreInscriptions - a.nombreInscriptions);
        break;
      case 'recent':
      default:
        filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        break;
    }

    return filtered;
  }

  switchTab(tab: 'tous' | 'publies' | 'brouillons') {
    this.activeTab = tab;
  }

  changeSortBy(sortBy: 'recent' | 'titre' | 'inscriptions') {
    this.sortBy = sortBy;
  }

  createNewParcours() {
    this.router.navigate(['/parcours/nouveau']);
  }

  editParcours(id: number) {
    this.router.navigate(['/parcours/modifier', id]);
  }

  manageParcours(id: number) {
    this.router.navigate(['/parcours/gerer', id]);
  }

  togglePublishParcours(parcours: ParcoursResponse) {
    if (!parcours.id) return;

    // Vérifier que le parcours a au moins une étape avant publication
    if (!parcours.isPublished && parcours.nombreEtapes === 0) {
      this.error = 'Un parcours doit avoir au moins une étape pour être publié';
      return;
    }

    const action = parcours.isPublished ? 'dépublier' : 'publier';
    if (!confirm(`Êtes-vous sûr de vouloir ${action} ce parcours ?`)) {
      return;
    }

    console.log('DEBUG: Tentative de publication du parcours:', parcours.id);
    console.log('DEBUG: Action:', action);
    
    this.parcoursService.togglePublishParcours(parcours.id).subscribe({
      next: (updatedParcours) => {
        console.log('DEBUG: Publication réussie:', updatedParcours);
        const index = this.parcours.findIndex(p => p.id === parcours.id);
        if (index !== -1) {
          this.parcours[index] = updatedParcours;
          this.calculateStats();
        }
        this.success = `Parcours ${action === 'publier' ? 'publié' : 'dépublié'} avec succès`;
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        console.error('DEBUG: Erreur complète:', err);
        console.error('DEBUG: Type d\'erreur:', typeof err);
        console.error('DEBUG: Status:', err.status);
        console.error('DEBUG: StatusText:', err.statusText);
        console.error('DEBUG: Error object:', err.error);
        console.error('DEBUG: Message:', err.message);
        console.error('DEBUG: URL:', err.url);
        
        // Extraire le message d'erreur du backend
        let errorMessage = `Erreur lors de la ${action === 'publier' ? 'publication' : 'dépublication'} du parcours`;
        
        if (err.error && typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        this.error = errorMessage;
        setTimeout(() => this.error = '', 5000);
      }
    });
  }

  deleteParcours(parcours: ParcoursResponse) {
    if (!parcours.id) return;

    if (!confirm(`Êtes-vous sûr de vouloir supprimer le parcours "${parcours.titre}" ? Cette action est irréversible.`)) {
      return;
    }

    this.parcoursService.deleteParcours(parcours.id).subscribe({
      next: () => {
        this.parcours = this.parcours.filter(p => p.id !== parcours.id);
        this.calculateStats();
        this.success = 'Parcours supprimé avec succès';
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression du parcours';
        console.error('Erreur:', err);
      }
    });
  }

  getNiveauDisplayName(niveau: NiveauDifficulte): string {
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getTypeParcoursDisplayName(type: TypeParcours): string {
    return this.parcoursService.getTypeParcoursDisplayName(type);
  }

  getProgressionColor(progression?: number): string {
    if (!progression) return 'bg-gray-200';
    if (progression < 30) return 'bg-red-500';
    if (progression < 70) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  getStatusBadgeClass(parcours: ParcoursResponse): string {
    if (parcours.isPublished) {
      return 'bg-green-100 text-green-800';
    }
    return 'bg-gray-100 text-gray-800';
  }

  getStatusText(parcours: ParcoursResponse): string {
    return parcours.isPublished ? 'Publié' : 'Brouillon';
  }

  clearMessages() {
    this.error = '';
    this.success = '';
  }
}