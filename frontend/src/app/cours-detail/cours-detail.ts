import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CoursService, Cours } from '../cours.service';
import { ModuleService, Module } from '../module.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { ModuleProgressService, ModuleProgress } from '../module-progress.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-cours-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cours-detail.html',
  styleUrls: ['./cours-detail.css']
})
export class CoursDetailComponent implements OnInit {
  cours: Cours | null = null;
  modules: Module[] = [];
  modulesProgress: ModuleProgress[] = [];
  coursId!: number;
  loading = false;
  error = '';
  success = '';
  enrollment: Enrollment | null = null;

  // Module form
  showModuleForm = false;
  editingModule: Module | null = null;
  moduleForm: Module = {
    titre: '',
    contenu: '',
    ordre: undefined
  };

  constructor(
    private coursService: CoursService,
    private moduleService: ModuleService,
    private enrollmentService: EnrollmentService,
    private moduleProgressService: ModuleProgressService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.coursId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCours();
    this.loadModules();
    if (!this.authService.isFormateur()) {
      this.loadEnrollment();
    }
  }

  loadEnrollment() {
    this.enrollmentService.getEnrollmentDetails(this.coursId).subscribe({
      next: (data) => {
        this.enrollment = data;
      },
      error: (err) => {
        // Pas inscrit, proposer l'inscription
        console.log('Pas inscrit à ce cours');
      }
    });
  }

  enrollInCourse() {
    this.enrollmentService.enrollInCourse(this.coursId).subscribe({
      next: () => {
        this.success = 'Inscription réussie !';
        this.loadEnrollment();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'inscription';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  loadCours() {
    this.loading = true;
    this.coursService.getCoursById(this.coursId).subscribe({
      next: (data) => {
        this.cours = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du cours';
        this.loading = false;
      }
    });
  }

  loadModules() {
    if (!this.authService.isFormateur()) {
      // Pour les étudiants, charger avec progression
      this.moduleProgressService.getModulesWithProgress(this.coursId).subscribe({
        next: (data) => {
          this.modulesProgress = data;
          this.modules = data; // Pour compatibilité avec le template
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des modules';
        }
      });
    } else {
      // Pour les formateurs, charger normalement
      this.moduleService.getModulesByCours(this.coursId).subscribe({
        next: (data) => {
          this.modules = data;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des modules';
        }
      });
    }
  }

  getModuleProgress(moduleId: number | undefined): ModuleProgress | null {
    if (!moduleId) return null;
    return this.modulesProgress.find(m => m.id === moduleId) || null;
  }

  isOwner(): boolean {
    return this.authService.isFormateur() && this.cours?.formateurId === this.getFormateurId();
  }

  getFormateurId(): number | null {
    // Cette méthode devrait récupérer l'ID du formateur depuis le profil
    // Pour l'instant, on va la laisser simple
    return null;
  }

  openModuleForm() {
    this.showModuleForm = true;
    this.editingModule = null;
    this.moduleForm = {
      titre: '',
      contenu: '',
      ordre: undefined
    };
    this.error = '';
    this.success = '';
  }

  editModule(module: Module) {
    this.showModuleForm = true;
    this.editingModule = module;
    this.moduleForm = { ...module };
    this.error = '';
    this.success = '';
  }

  cancelModuleForm() {
    this.showModuleForm = false;
    this.editingModule = null;
    this.moduleForm = {
      titre: '',
      contenu: '',
      ordre: undefined
    };
  }

  saveModule() {
    this.error = '';
    this.success = '';

    if (this.editingModule) {
      // Update
      this.moduleService.updateModule(this.editingModule.id!, this.moduleForm).subscribe({
        next: () => {
          this.success = 'Module modifié avec succès';
          this.loadModules();
          this.cancelModuleForm();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la modification du module';
        }
      });
    } else {
      // Create
      this.moduleService.createModule(this.coursId, this.moduleForm).subscribe({
        next: () => {
          this.success = 'Module ajouté avec succès';
          this.loadModules();
          this.cancelModuleForm();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de l\'ajout du module';
        }
      });
    }
  }

  deleteModule(moduleId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce module ?')) {
      this.moduleService.deleteModule(moduleId).subscribe({
        next: () => {
          this.success = 'Module supprimé avec succès';
          this.loadModules();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la suppression du module';
        }
      });
    }
  }

  goBack() {
    if (this.authService.isFormateur()) {
      this.router.navigate(['/formateur-dashboard']);
    } else {
      this.router.navigate(['/cours']);
    }
  }
}
