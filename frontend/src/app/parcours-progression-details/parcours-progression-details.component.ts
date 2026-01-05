import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { 
  ParcoursProgressionService, 
  ApprenantProgression, 
  StatistiquesGlobales 
} from '../services/parcours-progression.service';

@Component({
  selector: 'app-parcours-progression-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-progression-details.component.html',
  styleUrls: ['./parcours-progression-details.component.css']
})
export class ParcoursProgressionDetailsComponent implements OnInit {
  parcoursId!: number;
  progressions: ApprenantProgression[] = [];
  progressionsFiltrees: ApprenantProgression[] = [];
  statistiques: StatistiquesGlobales | null = null;
  loading = true;
  error = '';

  // Filtres
  filtreStatut = 'tous';
  rechercheTexte = '';
  triPar = 'nom';
  triOrdre = 'asc';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private progressionService: ParcoursProgressionService
  ) {}

  ngOnInit() {
    this.parcoursId = Number(this.route.snapshot.paramMap.get('id'));
    this.chargerDonnees();
    
    // Initialiser Feather icons
    this.initFeatherIcons();
  }

  private initFeatherIcons() {
    setTimeout(() => {
      if (typeof (window as any).feather !== 'undefined') {
        (window as any).feather.replace();
      }
    }, 100);
  }

  chargerDonnees() {
    this.loading = true;
    this.error = '';

    // Charger les statistiques globales
    this.progressionService.getStatistiquesGlobales(this.parcoursId).subscribe({
      next: (stats) => {
        this.statistiques = stats;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques:', err);
      }
    });

    // Charger les détails de progression
    this.progressionService.getProgressionDetails(this.parcoursId).subscribe({
      next: (progressions) => {
        this.progressions = progressions;
        this.appliquerFiltres();
        this.loading = false;
        
        // Réinitialiser les icônes après le chargement des données
        this.initFeatherIcons();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des progressions:', err);
        this.error = 'Erreur lors du chargement des données de progression';
        this.loading = false;
      }
    });
  }

  appliquerFiltres() {
    let resultats = [...this.progressions];

    // Filtre par statut
    if (this.filtreStatut !== 'tous') {
      resultats = resultats.filter(p => {
        switch (this.filtreStatut) {
          case 'termines': return p.isCompleted;
          case 'en-cours': return !p.isCompleted && p.progressionPourcentage > 0;
          case 'non-commences': return p.progressionPourcentage === 0;
          default: return true;
        }
      });
    }

    // Filtre par recherche
    if (this.rechercheTexte.trim()) {
      const terme = this.rechercheTexte.toLowerCase();
      resultats = resultats.filter(p => 
        p.nom.toLowerCase().includes(terme) ||
        p.prenom.toLowerCase().includes(terme) ||
        p.email.toLowerCase().includes(terme)
      );
    }

    // Tri
    resultats.sort((a, b) => {
      let valeurA: any, valeurB: any;
      
      switch (this.triPar) {
        case 'nom':
          valeurA = `${a.nom} ${a.prenom}`;
          valeurB = `${b.nom} ${b.prenom}`;
          break;
        case 'progression':
          valeurA = a.progressionPourcentage;
          valeurB = b.progressionPourcentage;
          break;
        case 'dateInscription':
          valeurA = new Date(a.dateInscription);
          valeurB = new Date(b.dateInscription);
          break;
        case 'points':
          valeurA = a.pointsGagnes;
          valeurB = b.pointsGagnes;
          break;
        default:
          return 0;
      }

      if (valeurA < valeurB) return this.triOrdre === 'asc' ? -1 : 1;
      if (valeurA > valeurB) return this.triOrdre === 'asc' ? 1 : -1;
      return 0;
    });

    this.progressionsFiltrees = resultats;
  }

  onFiltreChange() {
    this.appliquerFiltres();
  }

  changerTri(colonne: string) {
    if (this.triPar === colonne) {
      this.triOrdre = this.triOrdre === 'asc' ? 'desc' : 'asc';
    } else {
      this.triPar = colonne;
      this.triOrdre = 'asc';
    }
    this.appliquerFiltres();
  }

  getIconeTri(colonne: string): string {
    if (this.triPar !== colonne) return '↕️';
    return this.triOrdre === 'asc' ? '↑' : '↓';
  }

  contacterApprenant(email: string) {
    window.location.href = `mailto:${email}`;
  }

  voirDetailsApprenant(userId: number) {
    // Fonction supprimée - bouton retiré de l'interface
    console.log('Fonction désactivée');
  }

  exporterDonnees() {
    // Fonction supprimée - bouton retiré de l'interface  
    console.log('Fonction désactivée');
  }

  retourGestion() {
    this.router.navigate(['/parcours/gerer', this.parcoursId]);
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'Terminé': return 'statut-termine';
      case 'En cours': return 'statut-en-cours';
      case 'Non commencé': return 'statut-non-commence';
      default: return '';
    }
  }

  getProgressionClass(progression: number): string {
    if (progression >= 80) return 'progression-excellente';
    if (progression >= 60) return 'progression-bonne';
    if (progression >= 40) return 'progression-moyenne';
    return 'progression-faible';
  }

  getProgressionColor(progression: number): string {
    if (progression >= 80) return '#10b981'; // green-500
    if (progression >= 60) return '#3b82f6'; // blue-500
    if (progression >= 40) return '#eab308'; // yellow-500
    return '#ef4444'; // red-500
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('fr-FR');
  }
}