import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamificationService, BadgeResponse, BadgeRequest, PageResponse } from '../gamification.service';

@Component({
  selector: 'app-badge-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './badge-management.html',
  styleUrls: ['./badge-management.css']
})
export class BadgeManagementComponent implements OnInit {
  badges: BadgeResponse[] = [];
  loading = false;
  error = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Tri
  sortBy = 'createdAt';
  sortDir = 'desc';
  
  // Modal
  showModal = false;
  editingBadge: BadgeResponse | null = null;
  
  // Formulaire
  badgeForm: BadgeRequest = {
    name: '',
    description: '',
    iconUrl: '',
    criteriaType: 'COURS_COMPLETED',
    criteriaValue: 1,
    isActive: true
  };

  criteriaTypes = [
    { value: 'COURS_COMPLETED', label: 'Cours terminés' },
    { value: 'QUIZ_PASSED', label: 'Quiz réussis' },
    { value: 'PERFECT_SCORE', label: 'Score parfait' },
    { value: 'STREAK_DAYS', label: 'Jours consécutifs' },
    { value: 'XP_EARNED', label: 'Points XP gagnés' },
    { value: 'FIRST_COURSE', label: 'Premier cours' },
    { value: 'FIRST_QUIZ', label: 'Premier quiz' },
    { value: 'CHALLENGE_COMPLETED', label: 'Défi terminé' },
    { value: 'LEVEL_REACHED', label: 'Niveau atteint' }
  ];

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.loadBadges();
  }

  loadBadges() {
    this.loading = true;
    this.error = '';

    console.log('=== DEBUG: Loading badges ===');

    this.gamificationService.getAllBadges(this.currentPage, this.pageSize, this.sortBy, this.sortDir)
      .subscribe({
        next: (response: PageResponse<BadgeResponse>) => {
          console.log('=== DEBUG: Badges received ===', response);
          this.badges = response.content;
          
          // Debug chaque badge
          this.badges.forEach(badge => {
            console.log(`Badge ${badge.name}: isActive = ${badge.isActive} (type: ${typeof badge.isActive})`);
          });
          
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading badges:', error);
          this.error = 'Erreur lors du chargement des badges';
          this.loading = false;
        }
      });
  }

  openCreateModal() {
    this.editingBadge = null;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(badge: BadgeResponse) {
    this.editingBadge = badge;
    this.badgeForm = {
      name: badge.name,
      description: badge.description,
      iconUrl: badge.iconUrl,
      criteriaType: badge.criteriaType,
      criteriaValue: badge.criteriaValue,
      isActive: badge.isActive
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.editingBadge = null;
    this.resetForm();
  }

  resetForm() {
    this.badgeForm = {
      name: '',
      description: '',
      iconUrl: '',
      criteriaType: 'COURS_COMPLETED',
      criteriaValue: 1,
      isActive: true
    };
  }

  saveBadge() {
    if (!this.badgeForm.name.trim()) {
      this.error = 'Le nom du badge est requis';
      return;
    }

    this.loading = true;
    this.error = '';

    const request = this.editingBadge ? 
      this.gamificationService.updateBadge(this.editingBadge.id, this.badgeForm) :
      this.gamificationService.createBadge(this.badgeForm);

    request.subscribe({
      next: () => {
        this.closeModal();
        this.loadBadges();
      },
      error: (error) => {
        console.error('Error saving badge:', error);
        this.error = error.error?.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  deleteBadge(badge: BadgeResponse) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le badge "${badge.name}" ?`)) {
      return;
    }

    this.loading = true;
    this.gamificationService.deleteBadge(badge.id).subscribe({
      next: () => {
        this.loadBadges();
      },
      error: (error) => {
        console.error('Error deleting badge:', error);
        this.error = error.error?.message || 'Erreur lors de la suppression';
        this.loading = false;
      }
    });
  }

  toggleBadgeStatus(badge: BadgeResponse) {
    this.gamificationService.toggleBadgeStatus(badge.id).subscribe({
      next: () => {
        this.loadBadges();
      },
      error: (error) => {
        console.error('Error toggling badge status:', error);
        this.error = 'Erreur lors du changement de statut';
      }
    });
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadBadges();
  }

  changeSort(field: string) {
    if (this.sortBy === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadBadges();
  }

  getCriteriaTypeLabel(type: string): string {
    const criteria = this.criteriaTypes.find(c => c.value === type);
    return criteria ? criteria.label : type;
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR');
  }

  onImageError(event: any) {
    (event.target as HTMLImageElement).style.display = 'none';
  }

  isBadgeActive(badge: BadgeResponse): boolean {
    console.log(`Checking badge ${badge.name}: isActive = ${badge.isActive} (type: ${typeof badge.isActive})`);
    return !!badge.isActive;
  }
}