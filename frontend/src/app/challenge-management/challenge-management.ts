import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamificationService, ChallengeResponse, ChallengeRequest, PageResponse } from '../gamification.service';

@Component({
  selector: 'app-challenge-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './challenge-management.html',
  styleUrls: ['./challenge-management.css']
})
export class ChallengeManagementComponent implements OnInit {
  challenges: ChallengeResponse[] = [];
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
  editingChallenge: ChallengeResponse | null = null;
  
  // Formulaire
  challengeForm: ChallengeRequest = {
    name: '',
    description: '',
    challengeType: 'COMPLETE_COURSES',
    targetValue: 1,
    xpReward: 100,
    startDate: Date.now(),
    endDate: Date.now() + (7 * 24 * 60 * 60 * 1000), // 7 jours
    isActive: true
  };

  challengeTypes = [
    { 
      value: 'COMPLETE_COURSES', 
      label: 'Terminer des cours',
      description: 'Terminer un nombre spécifique de cours (total cumulé)',
      defaultTarget: 3,
      defaultReward: 150,
      targetLabel: 'Nombre de cours'
    },
    { 
      value: 'PASS_QUIZZES', 
      label: 'Réussir des quiz',
      description: 'Réussir un nombre spécifique de quiz (total cumulé)',
      defaultTarget: 5,
      defaultReward: 100,
      targetLabel: 'Nombre de quiz'
    },
    { 
      value: 'PERFECT_SCORES', 
      label: 'Scores parfaits',
      description: 'Obtenir un nombre de scores parfaits (100%)',
      defaultTarget: 2,
      defaultReward: 200,
      targetLabel: 'Nombre de scores parfaits'
    },
    { 
      value: 'DAILY_LOGIN', 
      label: 'Connexion quotidienne',
      description: 'Se connecter pendant un nombre de jours consécutifs',
      defaultTarget: 7,
      defaultReward: 250,
      targetLabel: 'Nombre de jours consécutifs'
    },
    { 
      value: 'EARN_BADGES', 
      label: 'Gagner des badges',
      description: 'Obtenir un nombre spécifique de badges différents',
      defaultTarget: 3,
      defaultReward: 300,
      targetLabel: 'Nombre de badges à obtenir'
    },
    { 
      value: 'COMPLETE_MODULE', 
      label: 'Terminer un module complet',
      description: 'Terminer entièrement un ou plusieurs modules (toutes les leçons + quiz)',
      defaultTarget: 1,
      defaultReward: 400,
      targetLabel: 'Nombre de modules complets'
    }
  ];

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.loadChallenges();
  }

  loadChallenges() {
    this.loading = true;
    this.error = '';

    this.gamificationService.getAllChallenges(this.currentPage, this.pageSize, this.sortBy, this.sortDir)
      .subscribe({
        next: (response: PageResponse<ChallengeResponse>) => {
          this.challenges = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading challenges:', error);
          this.error = 'Erreur lors du chargement des défis';
          this.loading = false;
        }
      });
  }

  openCreateModal() {
    this.editingChallenge = null;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(challenge: ChallengeResponse) {
    this.editingChallenge = challenge;
    this.challengeForm = {
      name: challenge.name,
      description: challenge.description,
      challengeType: challenge.challengeType,
      targetValue: challenge.targetValue,
      xpReward: challenge.xpReward,
      startDate: challenge.startDate,
      endDate: challenge.endDate,
      isActive: challenge.isActive
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.editingChallenge = null;
    this.resetForm();
  }

  resetForm() {
    const now = Date.now();
    this.challengeForm = {
      name: '',
      description: '',
      challengeType: 'COMPLETE_COURSES',
      targetValue: 1,
      xpReward: 100,
      startDate: now,
      endDate: now + (7 * 24 * 60 * 60 * 1000),
      isActive: true
    };
  }

  saveChallenge() {
    if (!this.challengeForm.name.trim()) {
      this.error = 'Le nom du défi est requis';
      return;
    }

    if (this.challengeForm.endDate <= this.challengeForm.startDate) {
      this.error = 'La date de fin doit être postérieure à la date de début';
      return;
    }

    this.loading = true;
    this.error = '';

    const request = this.editingChallenge ? 
      this.gamificationService.updateChallenge(this.editingChallenge.id, this.challengeForm) :
      this.gamificationService.createChallenge(this.challengeForm);

    request.subscribe({
      next: () => {
        this.closeModal();
        this.loadChallenges();
      },
      error: (error) => {
        console.error('Error saving challenge:', error);
        this.error = error.error?.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  deleteChallenge(challenge: ChallengeResponse) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le défi "${challenge.name}" ?`)) {
      return;
    }

    this.loading = true;
    this.gamificationService.deleteChallenge(challenge.id).subscribe({
      next: () => {
        this.loadChallenges();
      },
      error: (error) => {
        console.error('Error deleting challenge:', error);
        this.error = error.error?.message || 'Erreur lors de la suppression';
        this.loading = false;
      }
    });
  }

  toggleChallengeStatus(challenge: ChallengeResponse) {
    this.gamificationService.toggleChallengeStatus(challenge.id).subscribe({
      next: () => {
        this.loadChallenges();
      },
      error: (error) => {
        console.error('Error toggling challenge status:', error);
        this.error = 'Erreur lors du changement de statut';
      }
    });
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadChallenges();
  }

  changeSort(field: string) {
    if (this.sortBy === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadChallenges();
  }

  getChallengeTypeLabel(type: string): string {
    const challengeType = this.challengeTypes.find(c => c.value === type);
    return challengeType ? challengeType.label : type;
  }

  getChallengeTypeInfo(type: string) {
    return this.challengeTypes.find(c => c.value === type);
  }

  onChallengeTypeChange() {
    const typeInfo = this.getChallengeTypeInfo(this.challengeForm.challengeType);
    if (typeInfo) {
      this.challengeForm.targetValue = typeInfo.defaultTarget;
      this.challengeForm.xpReward = typeInfo.defaultReward;
    }
  }

  getTargetLabel(): string {
    const typeInfo = this.getChallengeTypeInfo(this.challengeForm.challengeType);
    return typeInfo?.targetLabel || 'Valeur cible';
  }

  getTypeDescription(): string {
    const typeInfo = this.getChallengeTypeInfo(this.challengeForm.challengeType);
    return typeInfo?.description || '';
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR');
  }

  formatDateTime(timestamp: number): string {
    return new Date(timestamp).toLocaleString('fr-FR');
  }

  isExpired(challenge: ChallengeResponse): boolean {
    return challenge.endDate < Date.now();
  }

  getStatusLabel(challenge: ChallengeResponse): string {
    if (!challenge.isActive) return 'Inactif';
    if (this.isExpired(challenge)) return 'Expiré';
    if (challenge.startDate > Date.now()) return 'À venir';
    return 'En cours';
  }

  getStatusClass(challenge: ChallengeResponse): string {
    if (!challenge.isActive) return 'inactive';
    if (this.isExpired(challenge)) return 'expired';
    if (challenge.startDate > Date.now()) return 'upcoming';
    return 'active';
  }

  getCompletionRate(challenge: ChallengeResponse): number {
    if (challenge.participantsCount === 0) return 0;
    return Math.round((challenge.completedCount / challenge.participantsCount) * 100);
  }

  formatDateTimeForInput(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toISOString().slice(0, 16);
  }

  parseDateTime(dateTimeString: string): number {
    return new Date(dateTimeString).getTime();
  }
}