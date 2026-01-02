import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UserAdminResponse, UserStatsResponse, PageResponse, BulkActionRequest } from '../admin.service';
import { AuthService } from '../auth';
import { Router } from '@angular/router';
import { UserModalComponent } from '../user-modal/user-modal';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, UserModalComponent],
  templateUrl: './admin-users.html',
  styleUrls: ['./admin-users.css']
})
export class AdminUsersComponent implements OnInit {
  users: UserAdminResponse[] = [];
  stats: UserStatsResponse | null = null;
  loading = false;
  error = '';
  success = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Sorting
  sortBy = 'createdAt';
  sortDir = 'desc';

  // Filters
  roleFilter = '';
  statusFilter = '';
  searchTerm = '';

  // Modal
  isModalOpen = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedUser: UserAdminResponse | null = null;

  // Selection
  selectedUsers: Set<number> = new Set();
  selectAll = false;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/home']);
      return;
    }
    
    this.loadUsers();
    this.loadStats();
  }

  loadUsers() {
    this.loading = true;
    this.error = '';

    this.adminService.getAllUsers(
      this.currentPage, 
      this.pageSize, 
      this.sortBy, 
      this.sortDir,
      this.searchTerm || undefined,
      this.roleFilter || undefined,
      this.statusFilter ? this.statusFilter === 'archived' : undefined
    ).subscribe({
      next: (response: PageResponse<UserAdminResponse>) => {
        this.users = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
        this.updateSelectAll();
      },
      error: (error) => {
        this.error = 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
        console.error('Error loading users:', error);
      }
    });
  }

  loadStats() {
    this.adminService.getUserStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Error loading stats:', error);
      }
    });
  }

  onSearchChange() {
    this.currentPage = 0;
    this.loadUsers();
  }

  onFilterChange() {
    this.currentPage = 0;
    this.loadUsers();
  }

  clearFilters() {
    this.searchTerm = '';
    this.roleFilter = '';
    this.statusFilter = '';
    this.currentPage = 0;
    this.loadUsers();
  }

  // Modal methods
  openCreateModal() {
    this.modalMode = 'create';
    this.selectedUser = null;
    this.isModalOpen = true;
  }

  openEditModal(user: UserAdminResponse) {
    this.modalMode = 'edit';
    this.selectedUser = user;
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.selectedUser = null;
  }

  onUserSaved(user: UserAdminResponse) {
    this.loadUsers();
    this.loadStats();
    this.success = `Utilisateur ${this.modalMode === 'create' ? 'créé' : 'modifié'} avec succès`;
    setTimeout(() => this.success = '', 3000);
  }

  // Selection methods
  toggleUserSelection(userId: number) {
    if (this.selectedUsers.has(userId)) {
      this.selectedUsers.delete(userId);
    } else {
      this.selectedUsers.add(userId);
    }
    this.updateSelectAll();
  }

  toggleSelectAll() {
    if (this.selectAll) {
      this.selectedUsers.clear();
    } else {
      this.users.forEach(user => this.selectedUsers.add(user.id));
    }
    this.selectAll = !this.selectAll;
  }

  updateSelectAll() {
    this.selectAll = this.users.length > 0 && this.users.every(user => this.selectedUsers.has(user.id));
  }

  isUserSelected(userId: number): boolean {
    return this.selectedUsers.has(userId);
  }

  getSelectedCount(): number {
    return this.selectedUsers.size;
  }

  // Bulk actions
  onBulkRoleChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.bulkChangeRole(target.value);
      target.value = ''; // Reset select
    }
  }

  getMaxDisplayed(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  getPageNumbers(): number[] {
    const maxPages = Math.min(5, this.totalPages);
    return Array.from({ length: maxPages }, (_, i) => i);
  }

  bulkArchive() {
    if (this.selectedUsers.size === 0) return;
    
    if (confirm(`Êtes-vous sûr de vouloir désactiver ${this.selectedUsers.size} utilisateur(s) ?`)) {
      this.executeBulkAction('archive');
    }
  }

  bulkActivate() {
    if (this.selectedUsers.size === 0) return;
    
    if (confirm(`Êtes-vous sûr de vouloir activer ${this.selectedUsers.size} utilisateur(s) ?`)) {
      this.executeBulkAction('activate');
    }
  }

  bulkChangeRole(newRole: string) {
    if (this.selectedUsers.size === 0) return;
    
    if (confirm(`Êtes-vous sûr de vouloir changer le rôle de ${this.selectedUsers.size} utilisateur(s) en ${newRole} ?`)) {
      this.executeBulkAction('change_role', newRole);
    }
  }

  bulkDelete() {
    if (this.selectedUsers.size === 0) return;
    
    if (confirm(`Êtes-vous sûr de vouloir archiver ${this.selectedUsers.size} utilisateur(s) ? Les comptes seront archivés mais les données seront conservées.`)) {
      this.executeBulkAction('archive');
    }
  }

  private executeBulkAction(action: string, newRole?: string) {
    const request: BulkActionRequest = {
      userIds: Array.from(this.selectedUsers),
      action,
      newRole
    };

    this.adminService.bulkAction(request).subscribe({
      next: () => {
        this.selectedUsers.clear();
        this.selectAll = false;
        this.loadUsers();
        this.loadStats();
        this.success = 'Action groupée exécutée avec succès';
        setTimeout(() => this.success = '', 3000);
      },
      error: (error) => {
        this.error = 'Erreur lors de l\'action groupée';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  // Individual actions
  onRoleChange(user: UserAdminResponse, event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.changeUserRole(user, target.value);
    }
  }

  changeUserRole(user: UserAdminResponse, newRole: string) {
    if (confirm(`Êtes-vous sûr de vouloir changer le rôle de ${user.firstName} ${user.lastName} en ${newRole} ?`)) {
      this.adminService.changeUserRole(user.id, newRole).subscribe({
        next: (updatedUser) => {
          const index = this.users.findIndex(u => u.id === user.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
          }
          this.success = 'Rôle modifié avec succès';
          this.loadStats();
          setTimeout(() => this.success = '', 3000);
        },
        error: (error) => {
          this.error = 'Erreur lors du changement de rôle';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  toggleUserStatus(user: UserAdminResponse) {
    const action = user.archived ? 'réactiver' : 'désactiver';
    if (confirm(`Êtes-vous sûr de vouloir ${action} le compte de ${user.firstName} ${user.lastName} ?`)) {
      this.adminService.toggleUserStatus(user.id, !user.archived).subscribe({
        next: (updatedUser) => {
          const index = this.users.findIndex(u => u.id === user.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
          }
          this.success = `Compte ${action} avec succès`;
          this.loadStats();
          setTimeout(() => this.success = '', 3000);
        },
        error: (error) => {
          this.error = `Erreur lors de la ${action}`;
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  unlockUser(user: UserAdminResponse) {
    if (confirm(`Êtes-vous sûr de vouloir déverrouiller le compte de ${user.firstName} ${user.lastName} ?`)) {
      this.adminService.unlockUserAccount(user.id).subscribe({
        next: () => {
          user.failedLoginAttempts = 0;
          user.accountLockedUntil = undefined;
          this.success = 'Compte déverrouillé avec succès';
          setTimeout(() => this.success = '', 3000);
        },
        error: (error) => {
          this.error = 'Erreur lors du déverrouillage';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  deleteUser(user: UserAdminResponse) {
    if (confirm(`Êtes-vous sûr de vouloir archiver ${user.firstName} ${user.lastName} ? Le compte sera archivé mais les données seront conservées.`)) {
      this.adminService.toggleUserStatus(user.id, true).subscribe({
        next: (updatedUser) => {
          const index = this.users.findIndex(u => u.id === user.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
          }
          this.success = 'Utilisateur archivé avec succès';
          this.loadStats();
          setTimeout(() => this.success = '', 3000);
        },
        error: (error) => {
          this.error = 'Erreur lors de l\'archivage';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  resetPassword(user: UserAdminResponse) {
    if (confirm(`Êtes-vous sûr de vouloir réinitialiser le mot de passe de ${user.firstName} ${user.lastName} ?`)) {
      this.adminService.resetUserPassword(user.id).subscribe({
        next: () => {
          this.success = 'Mot de passe réinitialisé avec succès';
          setTimeout(() => this.success = '', 3000);
        },
        error: (error) => {
          this.error = 'Erreur lors de la réinitialisation';
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  // Export
  exportUsers() {
    this.adminService.exportUsers().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `users_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        this.error = 'Erreur lors de l\'export';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadUsers();
  }

  onSortChange(sortBy: string) {
    if (this.sortBy === sortBy) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadUsers();
  }

  getFilteredUsers(): UserAdminResponse[] {
    return this.users;
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR');
  }

  isAccountLocked(user: UserAdminResponse): boolean {
    return user.accountLockedUntil ? user.accountLockedUntil > Date.now() : false;
  }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'role-admin';
      case 'FORMATEUR': return 'role-formateur';
      case 'ETUDIANT': return 'role-etudiant';
      default: return '';
    }
  }
}