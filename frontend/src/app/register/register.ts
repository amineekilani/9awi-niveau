import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  username = '';
  password = '';
  confirmPassword = '';
  error = '';
  success = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.authService.register({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.success = 'Inscription réussie ! Redirection...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        this.error = err.error || 'Erreur lors de l\'inscription';
      }
    });
  }
}