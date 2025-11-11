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
  username = '';
  email = '';
  password = '';
  confirmPassword = '';
  firstName = '';
  lastName = '';
  dateOfBirth = '';
  error = '';
  success = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.error = '';
    this.success = '';

    this.authService.register({ 
      username: this.username, 
      email: this.email, 
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      dateOfBirth: this.dateOfBirth
    }).subscribe({
      next: () => {
        // Auto-login after successful registration
        this.authService.login({ username: this.username, password: this.password }).subscribe({
          next: () => {
            this.router.navigate(['/home']);
          },
          error: () => {
            this.error = 'Inscription réussie mais erreur de connexion.';
            setTimeout(() => this.router.navigate(['/login']), 2000);
          }
        });
      },
      error: (err) => {
        console.error('Registration error:', err);
        this.error = 'Erreur lors de l\'inscription. Veuillez réessayer.';
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