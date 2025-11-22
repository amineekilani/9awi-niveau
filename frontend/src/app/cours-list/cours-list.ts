import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CoursService, Cours } from '../cours.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-cours-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cours-list.html',
  styleUrls: ['./cours-list.css']
})
export class CoursListComponent implements OnInit {
  cours: Cours[] = [];
  loading = false;
  error = '';

  constructor(
    private coursService: CoursService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadCours();
  }

  loadCours() {
    this.loading = true;
    this.coursService.getAllCours().subscribe({
      next: (data) => {
        this.cours = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
      }
    });
  }

  logout() {
    this.authService.logout();
  }
}
