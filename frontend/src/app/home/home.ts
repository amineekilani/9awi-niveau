import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

declare const feather: any;

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  email: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.email = this.authService.getEmail();
    
    // Rediriger selon le rôle
    if (this.authService.isFormateur()) {
      this.router.navigate(['/formateur-dashboard']);
    } else {
      this.router.navigate(['/cours']);
    }
  }

  logout() {
    this.authService.logout();
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  ngAfterViewInit(): void {
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
  }
}