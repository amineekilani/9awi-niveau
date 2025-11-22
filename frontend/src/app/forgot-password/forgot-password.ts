import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent {
  email: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;

    this.authService.forgotPassword(this.email).subscribe({
      next: (response: any) => {
        this.successMessage = response.message || 'Un email de réinitialisation a été envoyé si l\'adresse existe.';
        this.isLoading = false;
        this.email = '';
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Une erreur est survenue. Veuillez réessayer.';
        this.isLoading = false;
      }
    });
  }
}
