import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../auth';

declare const feather: any;

@Component({
  selector: 'app-confirm-delete',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-delete.html',
  styleUrls: ['./confirm-delete.css']
})
export class ConfirmDeleteComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';
  token = '';

  private apiUrl = 'http://localhost:8080/api/profile/confirm-delete';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (this.token) {
        this.confirmDeletion();
      } else {
        this.errorMessage = 'Token de suppression invalide';
        this.loading = false;
      }
    });
  }

  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  confirmDeletion() {
    this.http.delete(`${this.apiUrl}?token=${this.token}`, { 
      headers: this.getHeaders()
    }).subscribe({
      next: (response: any) => {
        this.success = true;
        this.loading = false;
        
        // Logout after 3 seconds
        setTimeout(() => {
          this.authService.logout();
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de la suppression du compte';
        this.loading = false;
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
