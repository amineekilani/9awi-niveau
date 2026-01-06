import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { CoursService, Cours } from '../cours.service';
import { ModuleService, Module } from '../module.service';
import { EnrollmentService, Enrollment } from '../enrollment.service';
import { ModuleProgressService, ModuleProgress } from '../module-progress.service';
import { ApprenantProgressionService, ApprenantProgression } from '../apprenant-progression.service';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';
import { NiveauBadgeComponent } from '../niveau-badge/niveau-badge';

declare const feather: any;

@Component({
  selector: 'app-cours-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent, NiveauBadgeComponent],
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
  apprenants: ApprenantProgression[] = [];
  showApprenants = false;

  // Données pour le header unifié
  userInitials = 'ET';
  userProfileImage = '';
  showNotifications = false;
  recentActivity: RecentActivity[] = [];
  userStats: UserGamificationStats | null = null;

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
    private apprenantProgressionService: ApprenantProgressionService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private gamificationService: UserGamificationService
  ) { }

  ngOnInit() {
    // Initialiser les données du header
    this.initHeaderData();

    this.coursId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCours();
    this.loadModules();
    if (!this.authService.isFormateur()) {
      this.loadEnrollment();
    } else {
      this.loadApprenants();
    }
  }

  loadApprenants() {
    this.apprenantProgressionService.getApprenantsProgression(this.coursId).subscribe({
      next: (data) => {
        this.apprenants = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des apprenants', err);
      }
    });
  }

  toggleApprenants() {
    this.showApprenants = !this.showApprenants;
  }

  formatDate(dateString: string | number): string {
    if (typeof dateString === 'number') {
      // Timestamp en millisecondes
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } else {
      // LocalDateTime string du backend
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
  }

  // Méthodes d'adaptation pour la compatibilité avec le template
  getProgressionGlobale(apprenant: any): number {
    return apprenant.progressionPourcentage || apprenant.progressionGlobale || 0;
  }

  getTotalLecons(apprenant: any): number {
    return apprenant.totalEtapes || apprenant.totalLecons || 0;
  }

  getLeconsCompletees(apprenant: any): number {
    // Utiliser etapeCourante qui contient maintenant le nombre réel de leçons complétées
    return apprenant.etapeCourante || apprenant.leconsCompletees || 0;
  }

  getEnrolledAt(apprenant: any): number {
    if (apprenant.dateInscription) {
      return new Date(apprenant.dateInscription).getTime();
    }
    return apprenant.enrolledAt || Date.now();
  }

  getLastAccessedAt(apprenant: any): number {
    if (apprenant.dateCompletion) {
      return new Date(apprenant.dateCompletion).getTime();
    }
    return apprenant.lastAccessedAt || Date.now();
  }

  getModulesProgression(apprenant: any): any[] {
    // Utiliser les étapes de progression du backend
    if (apprenant.etapesProgression && apprenant.etapesProgression.length > 0) {
      return apprenant.etapesProgression.map((etape: any) => ({
        moduleId: etape.etapeId,
        moduleTitre: etape.titreCours,
        totalLecons: 1, // Chaque module = 1 étape
        leconsCompletees: etape.isCompleted ? 1 : 0,
        progression: etape.isCompleted ? 100 : 0,
        quizResultat: etape.scoreObtenu ? {
          quizId: etape.etapeId,
          quizTitre: 'Quiz ' + etape.titreCours,
          meilleurScore: etape.scoreObtenu,
          nombreTentatives: 1,
          derniereTentative: etape.dateCompletion ? new Date(etape.dateCompletion).getTime() : Date.now(),
          passed: etape.scoreObtenu >= 50
        } : null
      }));
    }
    
    // Fallback: créer des modules fictifs basés sur la progression
    const totalModules = Math.max(1, Math.floor(this.getTotalLecons(apprenant) / 3)); // Estimer 3 leçons par module
    const completedModules = Math.floor((this.getLeconsCompletees(apprenant) / this.getTotalLecons(apprenant)) * totalModules);
    
    const modules = [];
    for (let i = 0; i < totalModules; i++) {
      const isCompleted = i < completedModules;
      modules.push({
        moduleId: i + 1,
        moduleTitre: `Module ${i + 1}`,
        totalLecons: Math.ceil(this.getTotalLecons(apprenant) / totalModules),
        leconsCompletees: isCompleted ? Math.ceil(this.getTotalLecons(apprenant) / totalModules) : 
                         (i === completedModules ? this.getLeconsCompletees(apprenant) % Math.ceil(this.getTotalLecons(apprenant) / totalModules) : 0),
        progression: isCompleted ? 100 : (i === completedModules ? ((this.getLeconsCompletees(apprenant) % Math.ceil(this.getTotalLecons(apprenant) / totalModules)) / Math.ceil(this.getTotalLecons(apprenant) / totalModules)) * 100 : 0),
        quizResultat: null
      });
    }
    
    return modules;
  }

  getProgressColor(progress: number): string {
    if (progress >= 80) return 'text-green-600';
    if (progress >= 50) return 'text-blue-600';
    if (progress >= 25) return 'text-yellow-600';
    return 'text-gray-600';
  }

  getProgressBarColor(progress: number): string {
    if (progress >= 80) return 'bg-green-600';
    if (progress >= 50) return 'bg-blue-600';
    if (progress >= 25) return 'bg-yellow-600';
    return 'bg-gray-600';
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
      this.router.navigate(['/mes-cours']);
    }
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
