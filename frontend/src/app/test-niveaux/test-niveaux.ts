import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CoursService, NiveauDifficulte, NiveauDifficulteInfo, Cours } from '../cours.service';
import { NiveauBadgeComponent } from '../niveau-badge/niveau-badge';

@Component({
  selector: 'app-test-niveaux',
  standalone: true,
  imports: [CommonModule, FormsModule, NiveauBadgeComponent],
  template: `
    <div class="container mx-auto p-6">
      <h1 class="text-3xl font-bold mb-6">Test des Niveaux de Difficulté</h1>
      
      <!-- Test des badges -->
      <div class="bg-white rounded-lg shadow p-6 mb-6">
        <h2 class="text-xl font-semibold mb-4">Badges de Niveau</h2>
        <div class="flex flex-wrap gap-4">
          <app-niveau-badge [niveau]="niveauDebutant"></app-niveau-badge>
          <app-niveau-badge [niveau]="niveauIntermediaire"></app-niveau-badge>
          <app-niveau-badge [niveau]="niveauAvance"></app-niveau-badge>
          <app-niveau-badge [niveau]="niveauExpert"></app-niveau-badge>
        </div>
      </div>

      <!-- Test des informations de niveau -->
      <div class="bg-white rounded-lg shadow p-6 mb-6">
        <h2 class="text-xl font-semibold mb-4">Informations des Niveaux</h2>
        <div *ngIf="loading" class="text-center py-4">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        </div>
        <div *ngIf="!loading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div *ngFor="let niveau of niveauxInfo" class="border rounded-lg p-4">
            <div class="flex items-center mb-2">
              <app-niveau-badge [niveau]="niveau.niveau"></app-niveau-badge>
            </div>
            <h3 class="font-semibold">{{ niveau.displayName }}</h3>
            <p class="text-sm text-gray-600 mb-2">{{ niveau.description }}</p>
            <div class="text-xs text-gray-500">
              <span class="inline-block bg-{{ niveau.badgeColor }}-100 text-{{ niveau.badgeColor }}-800 px-2 py-1 rounded">
                {{ niveau.badgeColor }}
              </span>
              <i class="fas fa-{{ niveau.icon }} ml-2"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Test de recherche par niveau -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4">Test de Recherche par Niveau</h2>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Sélectionner un niveau :</label>
          <select [(ngModel)]="selectedNiveau" (change)="searchByNiveau()" 
                  class="border border-gray-300 rounded-lg px-3 py-2">
            <option value="">Tous les niveaux</option>
            <option *ngFor="let niveau of niveauxInfo" [value]="niveau.niveau">
              {{ niveau.displayName }}
            </option>
          </select>
        </div>
        
        <div *ngIf="searchLoading" class="text-center py-4">
          <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
        </div>
        
        <div *ngIf="!searchLoading && coursParNiveau.length > 0">
          <h3 class="font-semibold mb-2">Cours trouvés ({{ coursParNiveau.length }}) :</h3>
          <div class="space-y-2">
            <div *ngFor="let cours of coursParNiveau" class="border rounded p-3">
              <div class="flex items-center justify-between">
                <h4 class="font-medium">{{ cours.titre }}</h4>
                <app-niveau-badge [niveau]="cours.niveauDifficulte"></app-niveau-badge>
              </div>
              <p class="text-sm text-gray-600">{{ cours.description }}</p>
            </div>
          </div>
        </div>
        
        <div *ngIf="!searchLoading && coursParNiveau.length === 0 && selectedNiveau">
          <p class="text-gray-500 text-center py-4">Aucun cours trouvé pour ce niveau.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      max-width: 1200px;
    }
  `]
})
export class TestNiveauxComponent implements OnInit {
  niveauDebutant = NiveauDifficulte.DEBUTANT;
  niveauIntermediaire = NiveauDifficulte.INTERMEDIAIRE;
  niveauAvance = NiveauDifficulte.AVANCE;
  niveauExpert = NiveauDifficulte.EXPERT;

  niveauxInfo: NiveauDifficulteInfo[] = [];
  loading = true;
  
  selectedNiveau = '';
  coursParNiveau: Cours[] = [];
  searchLoading = false;

  constructor(private coursService: CoursService) {}

  ngOnInit() {
    this.loadNiveauxInfo();
  }

  loadNiveauxInfo() {
    this.coursService.getNiveauxDifficulte().subscribe({
      next: (niveaux) => {
        this.niveauxInfo = niveaux;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des niveaux:', err);
        this.loading = false;
      }
    });
  }

  searchByNiveau() {
    if (!this.selectedNiveau) {
      this.coursParNiveau = [];
      return;
    }

    this.searchLoading = true;
    this.coursService.getCoursByNiveau(this.selectedNiveau as NiveauDifficulte).subscribe({
      next: (cours) => {
        this.coursParNiveau = cours;
        this.searchLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors de la recherche:', err);
        this.searchLoading = false;
      }
    });
  }
}