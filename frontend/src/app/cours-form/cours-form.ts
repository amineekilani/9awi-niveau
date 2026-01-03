import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CoursService, Cours } from '../cours.service';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';

declare const feather: any;

@Component({
  selector: 'app-cours-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cours-form.html',
  styleUrls: ['./cours-form.css']
})
export class CoursFormComponent implements OnInit {
  cours: Cours = {
    titre: '',
    description: '',
    categorie: ''
  };
  isEditMode = false;
  coursId?: number;
  loading = false;
  error = '';
  success = '';
  selectedFile: File | null = null;
  thumbnailPreview: string | null = null;
  uploadingThumbnail = false;

  // Données pour le header unifié
  userInitials = 'ET';
  userProfileImage = '';
  showNotifications = false;
  recentActivity: RecentActivity[] = [];
  userStats: UserGamificationStats | null = null;

  constructor(
    private coursService: CoursService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private userGamificationService: UserGamificationService
  ) { }

  ngOnInit() {
    if (!this.authService.isFormateur()) {
      this.router.navigate(['/home']);
      return;
    }

    // Initialiser les données du header
    this.initHeaderData();

    this.coursId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.coursId) {
      this.isEditMode = true;
      this.loadCours();
    }
  }

  loadCours() {
    if (!this.coursId) return;

    this.loading = true;
    this.coursService.getCoursById(this.coursId).subscribe({
      next: (data) => {
        this.cours = data;
        if (this.cours.thumbnailUrl) {
          this.thumbnailPreview = `http://localhost:8080/images/cours/${this.cours.thumbnailUrl}`;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du cours';
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Vérifier que c'est une image
      if (!file.type.startsWith('image/')) {
        this.error = 'Veuillez sélectionner une image';
        return;
      }

      // Vérifier la taille (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'L\'image ne doit pas dépasser 5MB';
        return;
      }

      this.selectedFile = file;

      // Prévisualisation
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.thumbnailPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  removeThumbnail() {
    this.selectedFile = null;
    this.thumbnailPreview = null;
    this.cours.thumbnailUrl = undefined;
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.success = '';

    // Si un fichier est sélectionné, l'uploader d'abord
    if (this.selectedFile) {
      this.uploadingThumbnail = true;
      this.coursService.uploadThumbnail(this.selectedFile).subscribe({
        next: (response) => {
          console.log('Upload response:', response);
          this.cours.thumbnailUrl = response.filename;
          this.uploadingThumbnail = false;
          this.saveCours();
        },
        error: (err) => {
          console.error('Upload error:', err);
          this.error = err.error?.message || 'Erreur lors de l\'upload de l\'image';
          this.loading = false;
          this.uploadingThumbnail = false;
        }
      });
    } else {
      this.saveCours();
    }
  }

  private saveCours() {
    const operation = this.isEditMode
      ? this.coursService.updateCours(this.coursId!, this.cours)
      : this.coursService.createCours(this.cours);

    operation.subscribe({
      next: () => {
        this.success = this.isEditMode ? 'Cours modifié avec succès' : 'Cours créé avec succès';
        setTimeout(() => {
          this.router.navigate(['/formateur-dashboard']);
        }, 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Une erreur est survenue';
        this.loading = false;
      }
    });
  }

  private initHeaderData() {
    this.authService.userProfile$.subscribe(profile => {
      if (profile) {
        this.userProfileImage = profile.profileImage || '';
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';
        if (firstName && lastName) {
          this.userInitials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        } else if (profile.email) {
          const namePart = profile.email.split('@')[0];
          this.userInitials = namePart.split('.').map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
      }
    });

    if (this.authService.getToken() && !this.userProfileImage) {
      this.authService.loadUserProfile();
    }

    this.userGamificationService.getRecentActivity(5).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
        setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
      }
    });

    this.userGamificationService.getUserStats().subscribe({
      next: (stats) => this.userStats = stats
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      setTimeout(() => { if (typeof feather !== 'undefined') feather.replace(); }, 100);
    }
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  logout() {
    this.authService.logout();
  }
}
