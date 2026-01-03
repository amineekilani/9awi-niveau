import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NiveauDifficulte } from '../cours.service';

@Component({
  selector: 'app-niveau-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span 
      class="badge niveau-badge niveau-{{ getBadgeClass() }}"
      [title]="getDescription()">
      <i class="fas fa-{{ getIcon() }}"></i>
      {{ getDisplayName() }}
    </span>
  `,
  styles: [`
    .niveau-badge {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      border-radius: 0.375rem;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
    }
    
    .niveau-DEBUTANT {
      background-color: #d1fae5;
      color: #065f46;
      border: 1px solid #a7f3d0;
    }
    
    .niveau-INTERMEDIAIRE {
      background-color: #fef3c7;
      color: #92400e;
      border: 1px solid #fde68a;
    }
    
    .niveau-AVANCE {
      background-color: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }
    
    .niveau-EXPERT {
      background-color: #e0e7ff;
      color: #3730a3;
      border: 1px solid #c7d2fe;
    }
    
    .niveau-badge i {
      font-size: 0.7rem;
    }
  `]
})
export class NiveauBadgeComponent {
  @Input() niveau!: NiveauDifficulte;

  getBadgeClass(): string {
    return this.niveau;
  }

  getDisplayName(): string {
    switch (this.niveau) {
      case NiveauDifficulte.DEBUTANT:
        return 'Débutant';
      case NiveauDifficulte.INTERMEDIAIRE:
        return 'Intermédiaire';
      case NiveauDifficulte.AVANCE:
        return 'Avancé';
      case NiveauDifficulte.EXPERT:
        return 'Expert';
      default:
        return 'Débutant';
    }
  }

  getDescription(): string {
    switch (this.niveau) {
      case NiveauDifficulte.DEBUTANT:
        return 'Aucun prérequis, concepts de base';
      case NiveauDifficulte.INTERMEDIAIRE:
        return 'Connaissances de base requises';
      case NiveauDifficulte.AVANCE:
        return 'Expérience significative nécessaire';
      case NiveauDifficulte.EXPERT:
        return 'Maîtrise complète du domaine';
      default:
        return 'Aucun prérequis, concepts de base';
    }
  }

  getIcon(): string {
    switch (this.niveau) {
      case NiveauDifficulte.DEBUTANT:
        return 'play-circle';
      case NiveauDifficulte.INTERMEDIAIRE:
        return 'chart-line';
      case NiveauDifficulte.AVANCE:
        return 'bolt';
      case NiveauDifficulte.EXPERT:
        return 'star';
      default:
        return 'play-circle';
    }
  }
}