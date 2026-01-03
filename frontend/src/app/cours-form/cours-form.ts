import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { CoursService, Cours } from '../cours.service';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';

declare const feather: any;

@Component({
  selector: 'app-cours-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
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

  categories: string[] = [];
  isCustomCategory = false;
  customCategory = '';

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

    this.loadCategories();
    this.coursId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.coursId) {
      this.isEditMode = true;
      this.loadCours();
    }
  }

  loadCategories() {
    this.coursService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        // Ajouter les catégories par défaut si elles n'existent pas
        const defaultCategories = [
          'Programmation', 'Design', 'Marketing', 'Business',
          'Langues', 'Sciences', 'Mathématiques', 'Développement Personnel'
        ];

        defaultCategories.forEach(cat => {
          if (!this.categories.includes(cat)) {
            this.categories.push(cat);
          }
        });

        this.categories.sort();
      },
      error: (err) => console.error('Erreur chargement catégories', err)
    });
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

        // Vérifier si la catégorie existe dans la liste
        if (this.cours.categorie && !this.categories.includes(this.cours.categorie)) {
          // Si la catégorie n'est pas dans la liste (ou pas encore chargée), on l'ajoute
          // Mais attendez, si elle n'est pas dans la liste des catégories par défaut + fetchées, c'est une custom ?
          // On va juste s'assurer qu'elle est dans la liste pour l'affichage correct du select
          if (!this.categories.includes(this.cours.categorie)) {
            this.categories.push(this.cours.categorie);
            this.categories.sort();
          }
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

    // Gérer la catégorie personnalisée
    if (this.isCustomCategory) {
      if (!this.customCategory.trim()) {
        this.error = 'Veuillez saisir une catégorie';
        this.loading = false;
        return;
      }
      this.cours.categorie = this.customCategory.trim();
    }

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

  onCategoryChange(event: any) {
    if (event.target.value === 'Autre') {
      this.isCustomCategory = true;
      this.cours.categorie = '';
    } else {
      this.isCustomCategory = false;
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
    // Redundant now
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
