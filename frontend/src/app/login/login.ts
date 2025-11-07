import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth';
import { CommonModule } from '@angular/common';

declare const feather: any;

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
  }
}