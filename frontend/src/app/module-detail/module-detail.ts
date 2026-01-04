import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ModuleService, Module } from '../module.service';
import { LeconService, Lecon } from '../lecon.service';
import { QuizService, Quiz, Question } from '../quiz.service';
import { EnrollmentService } from '../enrollment.service';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';

declare const feather: any;

@Component({
  selector: 'app-module-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './module-detail.html',
  styleUrls: ['./module-detail.css']
})
export class ModuleDetailComponent implements OnInit {
  module: Module | null = null;
  lecons: Lecon[] = [];
  quiz: Quiz | null = null;
  moduleId!: number;
  coursId!: number;
  uploadingFile = false;
  loading = false;
  success = '';
  error = '';
  
  // État des leçons complétées
  completedLeconIds: number[] = [];
  isEnrolled = false;

  // Données pour le header unifié
  userInitials = 'ET';
  userProfileImage = '';
  showNotifications = false;
  recentActivity: RecentActivity[] = [];
  userStats: UserGamificationStats | null = null;

  // Lecon form
  showLeconForm = false;
  editingLecon: Lecon | null = null;
  leconForm: Lecon = {
    titre: '',
    typeContenu: 'TEXTE'
  };
  selectedFile: File | null = null;

  // Quiz form
  showQuizForm = false;
  editingQuiz = false;
  quizForm: Quiz = {
    titre: ''
  };

  // Question form
  showQuestionForm = false;
  editingQuestion: Question | null = null;
  questionForm: Question = {
    question: '',
    options: ['', ''],
    correctAnswer: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private moduleService: ModuleService,
    private leconService: LeconService,
    private quizService: QuizService,
    private enrollmentService: EnrollmentService,
    public authService: AuthService,
    private gamificationService: UserGamificationService
  ) { }

  ngOnInit() {
    // Initialiser les données du header
    this.initHeaderData();

    this.route.params.subscribe(params => {
      this.moduleId = Number(params['id']);
      this.loadModule();
    });
  }

  loadModule() {
    this.loading = true;
    this.moduleService.getModuleById(this.moduleId).subscribe({
      next: (data) => {
        this.module = data;
        this.coursId = data.coursId!;
        this.loadLecons();
        this.loadQuiz();
        this.checkEnrollmentAndLoadProgress();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du module';
        this.loading = false;
      }
    });
  }

  checkEnrollmentAndLoadProgress() {
    if (!this.authService.isFormateur()) {
      this.enrollmentService.isEnrolled(this.coursId).subscribe({
        next: (enrolled) => {
          this.isEnrolled = enrolled;
          if (enrolled) {
            this.loadCompletedLecons();
          }
        },
        error: (err) => {
          console.error('Erreur lors de la vérification d\'inscription:', err);
        }
      });
    }
  }

  loadCompletedLecons() {
    this.enrollmentService.getCompletedLeconIds(this.coursId).subscribe({
      next: (completedIds) => {
        this.completedLeconIds = completedIds;
        console.log('Leçons complétées chargées:', completedIds);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des leçons complétées:', err);
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

  loadQuiz() {
    this.quizService.getQuizByModuleId(this.moduleId).subscribe({
      next: (data) => {
        this.quiz = data;
      },
      error: (err) => {
        // Pas de quiz pour ce module
        this.quiz = null;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  openLeconForm() {
    this.showLeconForm = true;
    this.editingLecon = null;
    this.leconForm = {
      titre: '',
      typeContenu: 'TEXTE'
    };
    this.selectedFile = null;
  }

  editLecon(lecon: Lecon) {
    this.showLeconForm = true;
    this.editingLecon = lecon;
    this.leconForm = { ...lecon };
    this.selectedFile = null;
  }

  cancelLeconForm() {
    this.showLeconForm = false;
    this.editingLecon = null;
  }

  saveLecon() {
    this.error = '';
    this.success = '';

    const saveOperation = () => {
      const operation = this.editingLecon
        ? this.leconService.updateLecon(this.editingLecon.id!, this.leconForm)
        : this.leconService.createLecon(this.moduleId, this.leconForm);

      operation.subscribe({
        next: () => {
          this.success = 'Leçon enregistrée avec succès';
          this.loadLecons();
          this.cancelLeconForm();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de l\'enregistrement de la leçon';
        }
      });
    };

    if (this.selectedFile) {
      this.uploadingFile = true;
      // Note: If leconService.uploadFile doesn't exist, we can use createLeconWithFile or updateLeconFile
      // Based on lecon.service.ts, they exist.
      const formData = new FormData();
      formData.append('file', this.selectedFile);
      formData.append('titre', this.leconForm.titre);
      formData.append('typeContenu', this.leconForm.typeContenu);
      if (this.leconForm.ordre) formData.append('ordre', this.leconForm.ordre.toString());
      if (this.leconForm.duree) formData.append('duree', this.leconForm.duree.toString());

      const operation = this.editingLecon
        ? this.leconService.updateLeconFile(this.editingLecon.id!, this.selectedFile)
        : this.leconService.createLeconWithFile(this.moduleId, formData);

      operation.subscribe({
        next: () => {
          this.success = 'Leçon enregistrée avec succès';
          this.uploadingFile = false;
          this.loadLecons();
          this.cancelLeconForm();
        },
        error: (err) => {
          this.error = 'Erreur lors de l\'upload du fichier';
          this.uploadingFile = false;
        }
      });
    } else {
      saveOperation();
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
          this.error = 'Erreur lors de la suppression de la leçon';
        }
      });
    }
  }

  getFileUrl(filename: string, type: string): string {
    return this.leconService.getFileUrl(filename, type);
  }

  onImageError(event: any) {
    event.target.src = 'assets/placeholder-image.png';
  }

  // Gestion de la completion des leçons
  toggleLeconCompletion(lecon: Lecon) {
    if (!this.isEnrolled || this.authService.isFormateur() || !lecon.id) {
      return;
    }

    const isCompleted = this.isLeconCompleted(lecon.id);
    
    if (isCompleted) {
      // Démarquer comme complétée
      this.enrollmentService.unmarkLeconAsCompleted(this.coursId, lecon.id).subscribe({
        next: (enrollment) => {
          this.completedLeconIds = this.completedLeconIds.filter(id => id !== lecon.id);
          this.success = 'Leçon marquée comme non complétée';
          console.log('Leçon démarquée:', lecon.id);
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = 'Erreur lors de la mise à jour de la progression';
          console.error('Erreur démarquage leçon:', err);
          setTimeout(() => this.error = '', 3000);
        }
      });
    } else {
      // Marquer comme complétée
      this.enrollmentService.markLeconAsCompleted(this.coursId, lecon.id).subscribe({
        next: (enrollment) => {
          this.completedLeconIds.push(lecon.id!);
          this.success = 'Leçon marquée comme complétée !';
          console.log('Leçon marquée:', lecon.id, 'Progression:', enrollment.progress + '%');
          setTimeout(() => this.success = '', 3000);
        },
        error: (err) => {
          this.error = 'Erreur lors de la mise à jour de la progression';
          console.error('Erreur marquage leçon:', err);
          setTimeout(() => this.error = '', 3000);
        }
      });
    }
  }

  isLeconCompleted(leconId: number): boolean {
    return this.completedLeconIds.includes(leconId);
  }

  allLeconsCompleted(): boolean {
    if (!this.isEnrolled || this.lecons.length === 0) return false;
    return this.lecons.every(lecon => lecon.id && this.isLeconCompleted(lecon.id));
  }

  openQuizForm() {
    this.showQuizForm = true;
    this.editingQuiz = this.quiz !== null;
    this.quizForm = this.quiz ? { ...this.quiz } : { titre: '' };
  }

  cancelQuizForm() {
    this.showQuizForm = false;
  }

  saveQuiz() {
    const operation = this.editingQuiz
      ? this.quizService.updateQuiz(this.quiz!.id!, this.quizForm)
      : this.quizService.createQuiz(this.moduleId, this.quizForm);

    operation.subscribe({
      next: () => {
        this.success = 'Quiz enregistré avec succès';
        this.loadQuiz();
        this.cancelQuizForm();
      },
      error: (err) => {
        this.error = 'Erreur lors de l\'enregistrement du quiz';
      }
    });
  }

  deleteQuiz() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce quiz ?')) {
      this.quizService.deleteQuiz(this.quiz!.id!).subscribe({
        next: () => {
          this.success = 'Quiz supprimé avec succès';
          this.quiz = null;
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression du quiz';
        }
      });
    }
  }

  openQuestionForm() {
    this.showQuestionForm = true;
    this.editingQuestion = null;
    this.questionForm = {
      question: '',
      options: ['', ''],
      correctAnswer: ''
    };
  }

  editQuestion(question: Question) {
    this.showQuestionForm = true;
    this.editingQuestion = question;
    this.questionForm = { ...question, options: [...question.options] };
  }

  cancelQuestionForm() {
    this.showQuestionForm = false;
    this.editingQuestion = null;
  }

  addOption() {
    this.questionForm.options.push('');
  }

  removeOption(index: number) {
    this.questionForm.options.splice(index, 1);
  }

  trackByIndex(index: number, obj: any): any {
    return index;
  }

  saveQuestion() {
    const operation = this.editingQuestion
      ? this.quizService.updateQuestion(this.editingQuestion.id!, this.questionForm)
      : this.quizService.addQuestion(this.quiz!.id!, this.questionForm);

    operation.subscribe({
      next: () => {
        this.success = 'Question enregistrée avec succès';
        this.loadQuiz();
        this.cancelQuestionForm();
      },
      error: (err) => {
        this.error = 'Erreur lors de l\'enregistrement de la question';
      }
    });
  }

  deleteQuestion(questionId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette question ?')) {
      this.quizService.deleteQuestion(questionId).subscribe({
        next: () => {
          this.success = 'Question supprimée avec succès';
          this.loadQuiz();
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression de la question';
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/cours', this.coursId]);
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
