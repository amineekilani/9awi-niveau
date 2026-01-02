import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamificationService, LevelResponse, LevelRequest, PageResponse } from '../gamification.service';

@Component({
  selector: 'app-level-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './level-management.html',
  styleUrls: ['./level-management.css']
})
export class LevelManagementComponent implements OnInit {
  levels: LevelResponse[] = [];
  loading = false;
  error = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Tri
  sortBy = 'level';
  sortDir = 'asc';
  
  // Modal
  showModal = false;
  editingLevel: LevelResponse | null = null;
  
  // Formulaire
  levelForm: LevelRequest = {
    level: 1,
    xpRequired: 0,
    name: '',
    description: ''
  };

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.loadLevels();
  }

  loadLevels() {
    this.loading = true;
    this.error = '';

    this.gamificationService.getAllLevels(this.currentPage, this.pageSize, this.sortBy, this.sortDir)
      .subscribe({
        next: (response: PageResponse<LevelResponse>) => {
          this.levels = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading levels:', error);
          this.error = 'Erreur lors du chargement des niveaux';
          this.loading = false;
        }
      });
  }

  openCreateModal() {
    this.editingLevel = null;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(level: LevelResponse) {
    this.editingLevel = level;
    this.levelForm = {
      level: level.level,
      xpRequired: level.xpRequired,
      name: level.name,
      description: level.description
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.editingLevel = null;
    this.resetForm();
  }

  resetForm() {
    // Calculer le prochain niveau disponible
    const maxLevel = this.levels.length > 0 ? Math.max(...this.levels.map(l => l.level)) : 0;
    const nextLevel = maxLevel + 1;
    
    this.levelForm = {
      level: nextLevel,
      xpRequired: this.calculateSuggestedXP(nextLevel),
      name: this.generateLevelName(nextLevel),
      description: ''
    };
  }

  calculateSuggestedXP(level: number): number {
    // Progression exponentielle suggérée
    if (level <= 1) return 0;
    return Math.round(Math.pow(level - 1, 2.2) * 100);
  }

  generateLevelName(level: number): string {
    const names = [
      '', 'Débutant', 'Apprenti', 'Étudiant', 'Avancé', 'Expert',
      'Maître', 'Sage', 'Légende', 'Champion', 'Grand Maître'
    ];
    
    if (level < names.length) {
      return names[level];
    }
    
    return `Niveau ${level}`;
  }

  saveLevel() {
    if (!this.levelForm.name.trim()) {
      this.error = 'Le nom du niveau est requis';
      return;
    }

    if (this.levelForm.level < 1) {
      this.error = 'Le numéro de niveau doit être supérieur à 0';
      return;
    }

    if (this.levelForm.xpRequired < 0) {
      this.error = 'Les XP requis ne peuvent pas être négatifs';
      return;
    }

    this.loading = true;
    this.error = '';

    const request = this.editingLevel ? 
      this.gamificationService.updateLevel(this.editingLevel.id, this.levelForm) :
      this.gamificationService.createLevel(this.levelForm);

    request.subscribe({
      next: () => {
        this.closeModal();
        this.loadLevels();
      },
      error: (error) => {
        console.error('Error saving level:', error);
        this.error = error.error?.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  deleteLevel(level: LevelResponse) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le niveau "${level.name}" ?`)) {
      return;
    }

    this.loading = true;
    this.gamificationService.deleteLevel(level.id).subscribe({
      next: () => {
        this.loadLevels();
      },
      error: (error) => {
        console.error('Error deleting level:', error);
        this.error = error.error?.message || 'Erreur lors de la suppression';
        this.loading = false;
      }
    });
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadLevels();
  }

  changeSort(field: string) {
    if (this.sortBy === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadLevels();
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR');
  }

  formatXP(xp: number): string {
    if (xp >= 1000000) {
      return (xp / 1000000).toFixed(1) + 'M';
    } else if (xp >= 1000) {
      return (xp / 1000).toFixed(1) + 'K';
    }
    return xp.toString();
  }

  getLevelColor(level: number): string {
    if (level >= 9) return '#ff6b35'; // Orange pour les niveaux très élevés
    if (level >= 7) return '#f7931e'; // Orange moyen
    if (level >= 5) return '#4caf50'; // Vert
    if (level >= 3) return '#2196f3'; // Bleu
    return '#9e9e9e'; // Gris pour les débutants
  }

  getProgressionInfo(level: LevelResponse): string {
    const currentLevel = this.levels.find(l => l.level === level.level);
    const nextLevel = this.levels.find(l => l.level === level.level + 1);
    
    if (!nextLevel) {
      return 'Niveau maximum';
    }
    
    const xpGap = nextLevel.xpRequired - level.xpRequired;
    return `+${this.formatXP(xpGap)} XP pour niveau suivant`;
  }
}