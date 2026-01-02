import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../auth';

declare const feather: any;

interface Profile {
  id?: number;
  email?: string;
  provider?: string;
  emailVerified?: boolean;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  profileImage?: string;
  role?: string;
  createdAt?: number;
  phoneNumber?: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit, AfterViewInit {
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
  // store only suffix (8 digits) without +216
  editPhoneNumberSuffix = '';
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  // Profile image
  selectedImageFile: File | null = null;
  profileImagePreview: string | null = null;
  uploadingImage = false;

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
    const headerObj: any = {};
    if (token) {
      headerObj['Authorization'] = `Bearer ${token}`;
    }
    return new HttpHeaders(headerObj);
  }

  loadProfile(): void {
    this.loading = true;
    this.http.get<Profile>(this.apiUrl, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.profile = data;
          this.editEmail = data.email || '';
          this.editFirstName = data.firstName || '';
          this.editLastName = data.lastName || '';
          this.editDateOfBirth = data.dateOfBirth || '';
          this.editPhoneNumberSuffix = data.phoneNumber ? data.phoneNumber.replace(/^\+216/, '') : '';
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors du chargement du profil';
          this.loading = false;
        }
      });
  }

  enableEditMode(): void {
    this.editMode = true;
    this.message = '';
    this.errorMessage = '';
  }

  cancelEdit(): void {
    this.editMode = false;
    if (this.profile) {
      this.editEmail = this.profile.email || '';
      this.editFirstName = this.profile.firstName || '';
      this.editLastName = this.profile.lastName || '';
      this.editDateOfBirth = this.profile.dateOfBirth || '';
      this.editPhoneNumberSuffix = this.profile.phoneNumber ? this.profile.phoneNumber.replace(/^\+216/, '') : '';
    }
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.message = '';
    this.errorMessage = '';
  }

  updateProfile(): void {
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

    // Include phone number (prefix +216 fixed)
    if (this.editPhoneNumberSuffix) {
      updateData.phoneNumber = '+216' + this.editPhoneNumberSuffix;
    }

    if (this.newPassword) {
      updateData.currentPassword = this.currentPassword;
      updateData.newPassword = this.newPassword;
    }

    this.loading = true;
    this.http.put(this.apiUrl, updateData, {
      headers: this.getHeaders()
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

  goBack(): void {
    this.router.navigate(['/home']);
  }

  /**
   * Gère la sélection d'une image de profil
   */
  onProfileImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validation de taille (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        this.errorMessage = 'L\'image ne doit pas dépasser 10MB';
        return;
      }

      this.selectedImageFile = file;

      // Créer une prévisualisation
      const reader = new FileReader();
      reader.onload = (e) => {
        this.profileImagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  clearProfileImage(event: Event): void {
    event.stopPropagation();
    this.selectedImageFile = null;
    this.profileImagePreview = null;
  }

  uploadProfileImage(): void {
    if (!this.selectedImageFile) {
      return;
    }

    this.uploadingImage = true;
    this.errorMessage = '';

    this.authService.uploadProfileImage(this.selectedImageFile).subscribe({
      next: () => {
        this.message = 'Photo de profil mise à jour avec succès';
        this.selectedImageFile = null;
        this.profileImagePreview = null;
        this.uploadingImage = false;
        this.loadProfile();
      },
      error: (err) => {
        console.error('Image upload error:', err);
        this.errorMessage = 'Erreur lors de l\'upload de l\'image. Veuillez réessayer.';
        this.uploadingImage = false;
      }
    });
  }

  // Format timestamp to French date like '22 Novembre 2025, 14:35'
  formatDateFrench(ts?: number): string {
    if (!ts) return '';
    const d = new Date(ts);
    const months = ['janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre'];
    const day = d.getDate();
    const month = months[d.getMonth()];
    const year = d.getFullYear();
    const hh = String(d.getHours()).padStart(2,'0');
    const mm = String(d.getMinutes()).padStart(2,'0');
    return `${day} ${month.charAt(0).toUpperCase()+month.slice(1)} ${year}, ${hh}:${mm}`;
  }

  formatPhoneSuffix(profile: Profile | null | undefined): string {
    if (!profile || !profile.phoneNumber) return 'Non renseigné';
    return profile.phoneNumber.replace(/^\+216/, '');
  }
}
