import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth';
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';

declare const feather: any;
declare const google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.error = ''; // Réinitialiser l'erreur
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => {
        console.log('Erreur complète:', err); // Pour déboguer
        
        // Vérifier d'abord si le backend a renvoyé un message
        if (err.error?.message) {
          this.error = err.error.message;
        } else if (err.status === 403) {
          this.error = 'Veuillez vérifier votre adresse email avant de vous connecter.';
        } else if (err.status === 401) {
          this.error = 'Nom d\'utilisateur ou mot de passe incorrect. Veuillez réessayer.';
        } else if (err.status === 0) {
          this.error = 'Impossible de se connecter au serveur.';
        } else {
          this.error = 'Une erreur est survenue lors de la connexion. Veuillez réessayer.';
        }
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
        document.getElementById('google-signin-button'),
        { 
          theme: 'outline', 
          size: 'large',
          width: '100%',
          text: 'signin_with',
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