import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ModuleService, Module } from '../module.service';
import { LeconService, Lecon } from '../lecon.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-module-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './module-detail.html',
  styleUrls: ['./module-detail.css']
})
export class ModuleDetailComponent implements OnInit {
  module: Module | null = null;
  lecons: Lecon[] = [];
  moduleId!: number;
  loading = false;
  error = '';
  success = '';

  // Lecon form
  showLeconForm = false;
  editingLecon: Lecon | null = null;
  leconForm: Lecon = {
    titre: '',
    typeContenu: 'TEXTE',
    contenuTexte: '',
    ordre: undefined,
    duree: undefined
  };
  selectedFile: File | null = null;

  constructor(
    private moduleService: ModuleService,
    private leconService: LeconService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.moduleId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadModule();
    this.loadLecons();
  }

  loadModule() {
    this.loading = true;
    this.moduleService.getModuleById(this.moduleId).subscribe({
      next: (data) => {
        this.module = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du module';
        this.loading = false;
      }
    });
  }

  loadLecons() {
    this.leconService.getLeconsByModule(this.moduleId).subscribe({
      next: (data) => {
        this.lecons = data;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des leçons';
      }
    });
  }

  openLeconForm() {
    this.showLeconForm = true;
    this.editingLecon = null;
    this.leconForm = {
      titre: '',
      typeContenu: 'TEXTE',
      contenuTexte: '',
      ordre: undefined,
      duree: undefined
    };
    this.selectedFile = null;
    this.error = '';
    this.success = '';
  }

  editLecon(lecon: Lecon) {
    this.showLeconForm = true;
    this.editingLecon = lecon;
    this.leconForm = { ...lecon };
    this.selectedFile = null;
    this.error = '';
    this.success = '';
  }

  cancelLeconForm() {
    this.showLeconForm = false;
    this.editingLecon = null;
    this.selectedFile = null;
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  saveLecon() {
    this.error = '';
    this.success = '';

    if (this.editingLecon) {
      // Update
      this.leconService.updateLecon(this.editingLecon.id!, this.leconForm).subscribe({
        next: () => {
          this.success = 'Leçon modifiée avec succès';
          this.loadLecons();
          this.cancelLeconForm();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la modification de la leçon';
        }
      });
    } else {
      // Create
      if (this.leconForm.typeContenu === 'TEXTE') {
        // Créer une leçon texte
        this.leconService.createLecon(this.moduleId, this.leconForm).subscribe({
          next: () => {
            this.success = 'Leçon ajoutée avec succès';
            this.loadLecons();
            this.cancelLeconForm();
          },
          error: (err) => {
            this.error = err.error?.message || 'Erreur lors de l\'ajout de la leçon';
          }
        });
      } else {
        // Créer une leçon avec fichier
        if (!this.selectedFile) {
          this.error = 'Veuillez sélectionner un fichier';
          return;
        }

        const formData = new FormData();
        formData.append('file', this.selectedFile);
        formData.append('titre', this.leconForm.titre);
        formData.append('typeContenu', this.leconForm.typeContenu);
        if (this.leconForm.ordre) {
          formData.append('ordre', this.leconForm.ordre.toString());
        }
        if (this.leconForm.duree) {
          formData.append('duree', this.leconForm.duree.toString());
        }

        this.leconService.createLeconWithFile(this.moduleId, formData).subscribe({
          next: () => {
            this.success = 'Leçon ajoutée avec succès';
            this.loadLecons();
            this.cancelLeconForm();
          },
          error: (err) => {
            this.error = err.error?.message || 'Erreur lors de l\'ajout de la leçon';
          }
        });
      }
    }
  }

  deleteLecon(leconId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette leçon ?')) {
      this.leconService.deleteLecon(leconId).subscribe({
        next: () => {
          this.success = 'Leçon supprimée avec succès';
          this.loadLecons();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la suppression de la leçon';
        }
      });
    }
  }

  getFileUrl(filename: string, typeContenu: string): string {
    return this.leconService.getFileUrl(filename, typeContenu);
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'TEXTE': return '📝';
      case 'PDF': return '📄';
      case 'IMAGE': return '🖼️';
      case 'VIDEO': return '🎥';
      default: return '📎';
    }
  }

  onImageError(event: any) {
    console.error('Erreur de chargement de l\'image:', event);
    this.error = 'Impossible de charger l\'image. Vérifiez que le fichier existe.';
  }

  goBack() {
    this.router.navigate(['/cours', this.module?.coursId]);
  }
}
