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
  error = '';
  success = '';
  
  // Profile image
  selectedImageFile: File | null = null;
  profileImagePreview: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

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

    this.error = '';
    this.success = '';

    // D'abord, enregistrer l'utilisateur
    this.authService.register({ 
      email: this.email, 
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      dateOfBirth: this.dateOfBirth
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
        this.error = 'Erreur lors de l\'inscription. Veuillez réessayer.';
      }
    });
  }

  uploadProfileImage(): void {
    if (!this.selectedImageFile) {
      return;
    }

    this.authService.uploadProfileImage(this.selectedImageFile).subscribe({
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