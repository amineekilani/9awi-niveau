import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, NiveauDifficulte } from '../parcours.service';
import { DOMAINES_SPECIALISATION } from '../constants/domaines';

declare const feather: any;

@Component({
  selector: 'app-parcours-catalogue',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-catalogue.component.html',
  styleUrls: ['./parcours-catalogue.component.css']
})
export class ParcoursCatalogueComponent implements OnInit {
  parcours: ParcoursResponse[] = [];
  parcoursFiltered: ParcoursResponse[] = [];
  loading = false;
  error = '';

  // Filtres
  searchTerm = '';
  selectedCategorie = '';
  selectedNiveau = '';
  sortBy = 'recent'; // recent, popular, alphabetical

  // Options de filtrage
  categories = DOMAINES_SPECIALISATION;
  niveaux = Object.values(NiveauDifficulte);

  // Vue
  viewMode = 'grid'; // grid ou list

  constructor(
    public parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
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

    this.parcoursService.getParcoursPublies().subscribe({
      next: (data) => {
        this.parcours = data;
        this.applyFilters();
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
        this.error = 'Erreur lors du chargement des parcours';
        this.loading = false;
      }
    });
  }

  applyFilters() {
    let filtered = [...this.parcours];

    // Filtre par terme de recherche
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(p => 
        p.titre.toLowerCase().includes(term) ||
        (p.description && p.description.toLowerCase().includes(term)) ||
        p.formateurNom.toLowerCase().includes(term)
      );
    }

    // Filtre par catégorie
    if (this.selectedCategorie) {
      filtered = filtered.filter(p => p.categorie === this.selectedCategorie);
    }

    // Filtre par niveau
    if (this.selectedNiveau) {
      filtered = filtered.filter(p => p.niveauDifficulte === this.selectedNiveau);
    }

    // Tri
    switch (this.sortBy) {
      case 'popular':
        filtered.sort((a, b) => b.nombreInscriptions - a.nombreInscriptions);
        break;
      case 'alphabetical':
        filtered.sort((a, b) => a.titre.localeCompare(b.titre));
        break;
      case 'recent':
      default:
        filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        break;
    }

    this.parcoursFiltered = filtered;
  }

  onSearchChange() {
    this.applyFilters();
  }

  onFilterChange() {
    this.applyFilters();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategorie = '';
    this.selectedNiveau = '';
    this.sortBy = 'recent';
    this.applyFilters();
  }

  toggleViewMode() {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }

  voirDetailsParcours(parcours: ParcoursResponse) {
    this.router.navigate(['/parcours', parcours.id]);
  }

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

  getProgressionColor(progression: number): string {
    if (progression >= 80) return 'bg-green-500';
    if (progression >= 50) return 'bg-yellow-500';
    return 'bg-blue-500';
  }
}