import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService, Profile } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';

declare const feather: any;
declare const VANTA: any;

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit, AfterViewInit {
  profile: Profile | null = null;
  loading = false;
  message = '';
  errorMessage = '';
  editMode = false;
  uploadingImage = false;

  // Données pour le header unifié
  userInitials = 'ET';
  userProfileImage = '';
  showNotifications = false;
  recentActivity: RecentActivity[] = [];
  userStats: UserGamificationStats | null = null;

  // Edit mode
  editEmail = '';
  editFirstName = '';
  editLastName = '';
  editDateOfBirth = '';
  editPhoneNumberSuffix = '';
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  // Profile image
  selectedImageFile: File | null = null;
  profileImagePreview: string | null = null;

  private apiUrl = 'http://localhost:8080/api/profile';

  constructor(
    private http: HttpClient,
    public authService: AuthService,
    private router: Router,
    private gamificationService: UserGamificationService
  ) { }

  ngOnInit() {
    this.loadProfile();
    this.initHeaderData();
    this.initVanta();
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  loadProfile() {
    this.loading = true;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    this.http.get<Profile>(this.apiUrl, { headers }).subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
        if (typeof feather !== 'undefined') {
          setTimeout(() => feather.replace(), 100);
        }
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du profil';
        this.loading = false;
      }
    });
  }

  enableEditMode() {
    if (this.profile) {
      this.editEmail = this.profile.email || '';
      this.editFirstName = this.profile.firstName || '';
      this.editLastName = this.profile.lastName || '';
      this.editDateOfBirth = this.profile.dateOfBirth || '';
      this.editPhoneNumberSuffix = this.profile.phoneNumber ? this.profile.phoneNumber.replace('+216', '') : '';
      this.editMode = true;
      if (typeof feather !== 'undefined') {
        setTimeout(() => feather.replace(), 100);
      }
    }
  }

  cancelEdit() {
    this.editMode = false;
    this.profileImagePreview = null;
    this.selectedImageFile = null;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  updateProfile() {
    if (this.newPassword && this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Les nouveaux mots de passe ne correspondent pas';
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    const updateData: any = {
      email: this.editEmail,
      firstName: this.editFirstName,
      lastName: this.editLastName,
      dateOfBirth: this.editDateOfBirth,
      phoneNumber: this.editPhoneNumberSuffix ? '+216' + this.editPhoneNumberSuffix : null
    };

    if (this.currentPassword && this.newPassword) {
      updateData.currentPassword = this.currentPassword;
      updateData.newPassword = this.newPassword;
    }

    this.loading = true;
    this.http.put<Profile>(this.apiUrl, updateData, { headers }).subscribe({
      next: (data) => {
        this.profile = data;
        this.message = 'Profil mis à jour avec succès';
        this.editMode = false;
        this.loading = false;
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.authService.loadUserProfile(); // Recharger le profil global
        if (typeof feather !== 'undefined') {
          setTimeout(() => feather.replace(), 100);
        }
      },
      error: (err) => {
        this.errorMessage = err.error.message || 'Erreur lors de la mise à jour du profil';
        this.loading = false;
      }
    });
  }

  onProfileImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedImageFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.profileImagePreview = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  clearProfileImage(event: Event) {
    event.stopPropagation();
    this.selectedImageFile = null;
    this.profileImagePreview = null;
  }

  uploadProfileImage() {
    if (!this.selectedImageFile) return;

    this.uploadingImage = true;
    const formData = new FormData();
    formData.append('file', this.selectedImageFile);

    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    this.http.post<Profile>(`${this.apiUrl}/image`, formData, { headers }).subscribe({
      next: (data) => {
        this.profile = data;
        this.message = 'Photo de profil mise à jour';
        this.selectedImageFile = null;
        this.profileImagePreview = null;
        this.uploadingImage = false;
        this.authService.loadUserProfile(); // Recharger le profil global
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de l\'envoi de l\'image';
        this.uploadingImage = false;
      }
    });
  }

  formatDateFrench(timestamp: any): string {
    if (!timestamp) return '';
    return new Date(timestamp).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  formatPhoneSuffix(profile: Profile | null): string {
    if (!profile || !profile.phoneNumber) return 'Non renseigné';
    return profile.phoneNumber.replace('+216', '');
  }

  goBack() {
    if (this.authService.isFormateur()) {
      this.router.navigate(['/formateur-dashboard']);
    } else {
      this.router.navigate(['/home']);
    }
  }

  private initVanta() {
    if (typeof VANTA !== 'undefined' && VANTA.NET) {
      VANTA.NET({
        el: "#vanta-bg",
        mouseControls: true,
        touchControls: true,
        gyroControls: false,
        minHeight: 200.00,
        minWidth: 200.00,
        scale: 1.00,
        scaleMobile: 1.00,
        color: 0x3b82f6,
        backgroundColor: 0xf8fafc,
        points: 10.00,
        maxDistance: 20.00,
        spacing: 15.00
      });
    }
  }

  private initHeaderData() {
    this.authService.userProfile$.subscribe(profile => {
      if (profile) {
        this.userProfileImage = profile.profileImage ? `http://localhost:8080/images/users/${profile.profileImage}` : '';
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';
        if (firstName && lastName) {
          this.userInitials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        } else if (profile.email) {
          const namePart = profile.email.split('@')[0];
          this.userInitials = namePart.split('.').map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
      }
    });

    if (this.authService.getToken() && !this.userProfileImage) {
      this.authService.loadUserProfile();
    }

    this.gamificationService.getRecentActivity(5).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
        setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
      }
    });

    this.gamificationService.getUserStats().subscribe({
      next: (stats) => this.userStats = stats
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
    }
  }

  logout() {
    this.authService.logout();
  }
}
