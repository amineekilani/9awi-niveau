import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth';
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';

declare const feather: any;
declare const google: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  email = '';
  password = '';
  confirmPassword = '';
  firstName = '';
  lastName = '';
  dateOfBirth = '';
  // Phone number split: prefix fixed, suffix for the 8 digits
  phoneNumberPrefix = '+216';
  phoneNumberSuffix = '';
  role = 'ETUDIANT'; // Par défaut étudiant
  domaineSpecialisation = ''; // Pour les formateurs
  error = '';
  success = '';

  // Profile image
  selectedImageFile: File | null = null;
  profileImagePreview: string | null = null;

  // Domaines disponibles
  domaines: any[] = [];

  constructor(private authService: AuthService, private router: Router) {
    this.loadDomaines();
  }

  loadDomaines(): void {
    this.authService.getDomaines().subscribe({
      next: (domaines) => {
        this.domaines = domaines;
        console.log('Domaines chargés:', domaines); // Debug
      },
      error: (err) => {
        console.error('Erreur lors du chargement des domaines:', err);
      }
    });
  }

  onProfileImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validation de taille (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        this.error = 'L\'image ne doit pas dépasser 10MB';
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

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    // Validation date de naissance
    const birthYear = new Date(this.dateOfBirth).getFullYear();
    if (birthYear < 1906 || birthYear > 2020) {
      this.error = 'La date de naissance est invalide';
      return;
    }

    // Validation pour les formateurs
    if (this.role === 'FORMATEUR' && (!this.domaineSpecialisation || this.domaineSpecialisation.trim() === '')) {
      this.error = 'Veuillez sélectionner un domaine de spécialisation';
      return;
    }

    this.error = '';
    this.success = '';

    // D'abord, enregistrer l'utilisateur
    const phoneNumber = this.phoneNumberPrefix + (this.phoneNumberSuffix || '');

    this.authService.register({
      email: this.email,
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      dateOfBirth: this.dateOfBirth,
      phoneNumber: phoneNumber,
      role: this.role,
      domaineSpecialisation: this.role === 'FORMATEUR' ? this.domaineSpecialisation : undefined
    }).subscribe({
      next: () => {
        // Si une image a été sélectionnée, l'uploader après l'inscription
        if (this.selectedImageFile) {
          this.uploadProfileImage();
        } else {
          this.success = 'Un mail a été envoyé, veuillez confirmer votre adresse.';
          setTimeout(() => this.router.navigate(['/login']), 3000);
        }
      },
      error: (err) => {
        console.error('Registration error:', err);
        try {
          // Essayer de parser err.error si c'est une chaîne JSON
          let errorMessage = '';
          if (typeof err.error === 'string') {
            const parsedError = JSON.parse(err.error);
            errorMessage = parsedError.message;
          } else if (err.error?.message) {
            errorMessage = err.error.message;
          }
          
          this.error = errorMessage || 'Erreur lors de l\'inscription. Veuillez réessayer.';
        } catch (parseError) {
          console.error('Error parsing error response:', parseError);
          this.error = 'Erreur lors de l\'inscription. Veuillez réessayer.';
        }
      }
    });
  }

  uploadProfileImage(): void {
    if (!this.selectedImageFile) {
      return;
    }

    this.authService.uploadProfileImageAfterRegister(this.selectedImageFile, this.email).subscribe({
      next: () => {
        this.success = 'Un mail a été envoyé, veuillez confirmer votre adresse.';
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        console.error('Image upload error:', err);
        // Même si l'upload d'image échoue, rediriger vers login
        this.success = 'Un mail a été envoyé, veuillez confirmer votre adresse.';
        setTimeout(() => this.router.navigate(['/login']), 3000);
      }
    });
  }

  ngAfterViewInit(): void {
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
    this.initializeGoogleSignIn();
  }

  initializeGoogleSignIn(): void {
    if (typeof google !== 'undefined') {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => this.handleGoogleSignIn(response)
      });

      google.accounts.id.renderButton(
        document.getElementById('google-signup-button'),
        {
          theme: 'outline',
          size: 'large',
          width: '100%',
          text: 'signup_with',
          locale: 'fr'
        }
      );
    }
  }

  handleGoogleSignIn(response: any): void {
    this.error = '';
    this.authService.loginWithGoogle(response.credential).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => {
        console.log('Erreur Google OAuth:', err);
        if (err.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Échec de l\'authentification Google. Veuillez réessayer.';
        }
      }
    });
  }
}