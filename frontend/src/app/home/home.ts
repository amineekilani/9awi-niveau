import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

declare const feather: any;

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  username: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.username = this.authService.getUsername();
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