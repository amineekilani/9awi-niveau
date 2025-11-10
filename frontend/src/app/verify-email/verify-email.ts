import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verify-email.html',
  styleUrls: ['./verify-email.css']
})
export class VerifyEmailComponent implements OnInit {
  message: string = '';
  isLoading: boolean = true;
  isSuccess: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    
    if (!token) {
      this.message = 'Token de vérification manquant';
      this.isLoading = false;
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response: any) => {
        this.message = response.message || 'Email vérifié avec succès !';
        this.isSuccess = true;
        this.isLoading = false;
        
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (error) => {
        this.message = error.error?.message || 'Erreur lors de la vérification de l\'email';
        this.isSuccess = false;
        this.isLoading = false;
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
