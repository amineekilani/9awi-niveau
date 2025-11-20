import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../auth';

declare const feather: any;

interface Profile {
  id: number;
  email: string;
  provider: string;
  emailVerified: boolean;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  profile: Profile | null = null;
  loading = false;
  message = '';
  errorMessage = '';
  
  // Edit mode
  editMode = false;
  editEmail = '';
  editFirstName = '';
  editLastName = '';
  editDateOfBirth = '';
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  
  // Delete mode
  deleteMode = false;
  deleteEmail = '';
  
  private apiUrl = 'http://localhost:8080/api/profile';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadProfile();
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadProfile() {
    this.loading = true;
    this.http.get<Profile>(this.apiUrl, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.profile = data;
          this.editEmail = data.email;
          this.editFirstName = data.firstName || '';
          this.editLastName = data.lastName || '';
          this.editDateOfBirth = data.dateOfBirth || '';
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Erreur lors du chargement du profil';
          this.loading = false;
        }
      });
  }

  enableEditMode() {
    this.editMode = true;
    this.message = '';
    this.errorMessage = '';
  }

  cancelEdit() {
    this.editMode = false;
    if (this.profile) {
      this.editEmail = this.profile.email;
      this.editFirstName = this.profile.firstName || '';
      this.editLastName = this.profile.lastName || '';
      this.editDateOfBirth = this.profile.dateOfBirth || '';
    }
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.message = '';
    this.errorMessage = '';
  }

  updateProfile() {
    this.message = '';
    this.errorMessage = '';

    // Validate password change
    if (this.newPassword) {
      if (!this.currentPassword) {
        this.errorMessage = 'Veuillez entrer votre mot de passe actuel';
        return;
      }
      if (this.newPassword !== this.confirmPassword) {
        this.errorMessage = 'Les mots de passe ne correspondent pas';
        return;
      }
      if (this.newPassword.length < 6) {
        this.errorMessage = 'Le nouveau mot de passe doit contenir au moins 6 caractères';
        return;
      }
    }

    const updateData: any = {
      email: this.editEmail,
      firstName: this.editFirstName,
      lastName: this.editLastName,
      dateOfBirth: this.editDateOfBirth
    };

    if (this.newPassword) {
      updateData.currentPassword = this.currentPassword;
      updateData.newPassword = this.newPassword;
    }

    this.loading = true;
    this.http.put(this.apiUrl, updateData, { 
      headers: this.getHeaders(),
      responseType: 'json'
    }).subscribe({
      next: (response: any) => {
        this.message = response.message || 'Profil mis à jour avec succès';
        this.editMode = false;
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.loadProfile();
        
        // Update email in localStorage if changed
        if (this.editEmail !== this.authService.getEmail()) {
          localStorage.setItem('auth-email', this.editEmail);
        }
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de la mise à jour du profil';
        this.loading = false;
      }
    });
  }

  enableDeleteMode() {
    this.deleteMode = true;
    this.deleteEmail = '';
    this.message = '';
    this.errorMessage = '';
  }

  cancelDelete() {
    this.deleteMode = false;
    this.deleteEmail = '';
    this.message = '';
    this.errorMessage = '';
  }

  requestAccountDeletion() {
    if (!this.deleteEmail) {
      this.errorMessage = 'Veuillez entrer votre email';
      return;
    }

    if (this.deleteEmail !== this.profile?.email) {
      this.errorMessage = 'L\'email ne correspond pas';
      return;
    }

    this.loading = true;
    this.http.post(`${this.apiUrl}/request-delete`, 
      { email: this.deleteEmail },
      { headers: this.getHeaders() }
    ).subscribe({
      next: (response: any) => {
        this.message = response.message || 'Email de confirmation envoyé';
        this.deleteMode = false;
        this.deleteEmail = '';
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de la demande de suppression';
        this.loading = false;
      }
    });
  }

  goBack() {
    this.router.navigate(['/home']);
  }
}
