import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../auth';
import { AdminService, UserStatsResponse } from '../admin.service';
import { GamificationService, GamificationStatsResponse } from '../gamification.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-admin-main',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './admin-main.html',
  styleUrls: ['./admin-main.css']
})
export class AdminMainComponent implements OnInit {
  currentRoute = '/admin/dashboard';

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/home']);
      return;
    }

    // Écouter les changements de route
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.currentRoute = event.url;
      }
    });

    // Rediriger vers dashboard par défaut
    if (this.router.url === '/admin') {
      this.router.navigate(['/admin/dashboard']);
    }
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  logout() {
    this.authService.logout();
  }

  getUserInitials(): string {
    const email = this.authService.getEmail();
    if (email) {
      return email.charAt(0).toUpperCase();
    }
    return 'A';
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (target) {
      target.style.display = 'none';
    }
  }
}