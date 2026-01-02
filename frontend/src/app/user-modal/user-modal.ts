import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UserAdminResponse, CreateUserRequest, UpdateUserRequest } from '../admin.service';

@Component({
  selector: 'app-user-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-modal.html',
  styleUrls: ['./user-modal.css']
})
export class UserModalComponent implements OnInit, OnChanges {
  @Input() isOpen = false;
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() user: UserAdminResponse | null = null;
  @Output() closeModal = new EventEmitter<void>();
  @Output() userSaved = new EventEmitter<UserAdminResponse>();

  formData = {
    email: '',
    firstName: '',
    lastName: '',
    role: 'ETUDIANT',
    phoneNumber: '',
    dateOfBirth: '',
    emailVerified: false
  };

  loading = false;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadUserData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] || changes['mode'] || changes['isOpen']) {
      this.loadUserData();
    }
  }

  private loadUserData() {
    if (this.mode === 'edit' && this.user) {
      this.formData = {
        email: this.user.email,
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        role: this.user.role,
        phoneNumber: this.user.phoneNumber || '',
        dateOfBirth: this.user.dateOfBirth || '',
        emailVerified: this.user.emailVerified
      };
    } else {
      this.resetForm();
    }
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.error = '';

    if (this.mode === 'create') {
      this.createUser();
    } else {
      this.updateUser();
    }
  }

  private createUser() {
    const request: CreateUserRequest = {
      email: this.formData.email,
      firstName: this.formData.firstName,
      lastName: this.formData.lastName,
      role: this.formData.role,
      phoneNumber: this.formData.phoneNumber || undefined,
      dateOfBirth: this.formData.dateOfBirth || undefined,
      emailVerified: this.formData.emailVerified
    };

    this.adminService.createUser(request).subscribe({
      next: (user) => {
        this.userSaved.emit(user);
        this.close();
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Erreur lors de la création';
        this.loading = false;
      }
    });
  }

  private updateUser() {
    if (!this.user) return;

    const request: UpdateUserRequest = {
      firstName: this.formData.firstName,
      lastName: this.formData.lastName,
      phoneNumber: this.formData.phoneNumber || undefined,
      dateOfBirth: this.formData.dateOfBirth || undefined,
      emailVerified: this.formData.emailVerified
    };

    this.adminService.updateUser(this.user.id, request).subscribe({
      next: (user) => {
        this.userSaved.emit(user);
        this.close();
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Erreur lors de la mise à jour';
        this.loading = false;
      }
    });
  }

  private validateForm(): boolean {
    if (!this.formData.firstName.trim()) {
      this.error = 'Le prénom est requis';
      return false;
    }
    if (!this.formData.lastName.trim()) {
      this.error = 'Le nom est requis';
      return false;
    }
    if (this.mode === 'create' && !this.formData.email.trim()) {
      this.error = 'L\'email est requis';
      return false;
    }
    if (this.mode === 'create' && !this.isValidEmail(this.formData.email)) {
      this.error = 'Format d\'email invalide';
      return false;
    }
    return true;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  close() {
    this.isOpen = false;
    this.closeModal.emit();
    this.resetForm();
  }

  private resetForm() {
    this.formData = {
      email: '',
      firstName: '',
      lastName: '',
      role: 'ETUDIANT',
      phoneNumber: '',
      dateOfBirth: '',
      emailVerified: false
    };
    this.error = '';
    this.loading = false;
  }

  onBackdropClick(event: Event) {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }
}