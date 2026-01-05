import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ModuleService, Module } from '../module.service';
import { LeconService, Lecon } from '../lecon.service';
import { QuizService, Quiz, Question } from '../quiz.service';
import { ExerciceService, Exercice, ExerciceElement } from '../exercice.service';
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
  exercice: Exercice | null = null;
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

  // Exercice form
  showExerciceForm = false;
  editingExercice = false;
  exerciceForm: Exercice = {
    titre: '',
    typeExercice: 'FILL_BLANK'
  };

  // Pour le texte à trous
  fillBlankText = '';
  
  // Pour drag and drop
  draggableItems: string[] = [''];
  dropZones: { label: string; correctAnswer: string }[] = [{ label: '', correctAnswer: '' }];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private moduleService: ModuleService,
    private leconService: LeconService,
    private quizService: QuizService,
    private exerciceService: ExerciceService,
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
        this.loadExercice();
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

  loadExercice() {
    this.exerciceService.getExerciceByModuleId(this.moduleId).subscribe({
      next: (data) => {
        this.exercice = data;
      },
      error: (err) => {
        // Pas d'exercice pour ce module
        this.exercice = null;
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

  // Méthodes pour les exercices
  openExerciceForm() {
    this.showExerciceForm = true;
    this.editingExercice = this.exercice !== null;
    this.exerciceForm = this.exercice ? { ...this.exercice } : { titre: '', typeExercice: 'FILL_BLANK' };
    this.resetExerciceFormData();
  }

  cancelExerciceForm() {
    this.showExerciceForm = false;
    this.resetExerciceFormData();
  }

  resetExerciceFormData() {
    this.fillBlankText = '';
    this.dropZones = [{ label: '', correctAnswer: '' }];
  }

  onExerciceTypeChange() {
    this.resetExerciceFormData();
  }

  // Méthodes pour texte à trous
  generateFillBlankElements(): ExerciceElement[] {
    const elements: ExerciceElement[] = [];
    let position = 1;
    
    // Utiliser une regex sans groupe de capture pour éviter les éléments supplémentaires
    const parts = this.fillBlankText.split(/(\[BLANK:[^\]]+\])/);
    
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      
      if (part.startsWith('[BLANK:')) {
        const match = part.match(/\[BLANK:([^\]]+)\]/);
        if (match) {
          elements.push({
            contenu: '',
            typeElement: 'BLANK',
            positionOrdre: position++,
            reponseCorrecte: match[1].trim()
          });
        }
      } else if (part && part.trim() !== '') {
        // Ajouter seulement les parties non vides qui ne sont pas des BLANK
        elements.push({
          contenu: part,
          typeElement: 'TEXT',
          positionOrdre: position++
        });
      }
    }
    
    return elements;
  }

  // Méthodes pour drag and drop
  addDraggableItem() {
    this.draggableItems.push('');
  }

  removeDraggableItem(index: number) {
    this.draggableItems.splice(index, 1);
  }

  addDropZone() {
    this.dropZones.push({ label: '', correctAnswer: '' });
  }

  removeDropZone(index: number) {
    this.dropZones.splice(index, 1);
  }

  generateDragDropElements(): ExerciceElement[] {
    const elements: ExerciceElement[] = [];
    let position = 1;

    // Créer les éléments DRAGGABLE à partir des réponses correctes
    this.dropZones.forEach(zone => {
      if (zone.correctAnswer.trim()) {
        elements.push({
          contenu: zone.correctAnswer.trim(),
          typeElement: 'DRAGGABLE',
          positionOrdre: position++
        });
      }
    });

    // Créer les zones de dépôt avec les définitions
    this.dropZones.forEach(zone => {
      if (zone.label.trim() && zone.correctAnswer.trim()) {
        elements.push({
          contenu: zone.label.trim(), // La définition
          typeElement: 'DROP_ZONE',
          positionOrdre: position++,
          reponseCorrecte: zone.correctAnswer.trim() // Le terme correspondant
        });
      }
    });

    return elements;
  }

  saveExercice() {
    if (!this.exerciceForm.titre.trim()) {
      this.error = 'Le titre est obligatoire';
      return;
    }

    // Générer les éléments selon le type
    let elements: ExerciceElement[] = [];
    switch (this.exerciceForm.typeExercice) {
      case 'FILL_BLANK':
        elements = this.generateFillBlankElements();
        break;
      case 'DRAG_DROP':
        elements = this.generateDragDropElements();
        break;
    }

    if (elements.length === 0) {
      this.error = 'L\'exercice doit contenir au moins un élément';
      return;
    }

    this.exerciceForm.elements = elements;

    const operation = this.editingExercice
      ? this.exerciceService.updateExercice(this.exercice!.id!, this.exerciceForm)
      : this.exerciceService.createExercice(this.moduleId, this.exerciceForm);

    operation.subscribe({
      next: () => {
        this.success = 'Exercice enregistré avec succès';
        this.loadExercice();
        this.cancelExerciceForm();
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement de l\'exercice';
      }
    });
  }

  deleteExercice() {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet exercice ?')) {
      this.exerciceService.deleteExercice(this.exercice!.id!).subscribe({
        next: () => {
          this.success = 'Exercice supprimé avec succès';
          this.exercice = null;
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression de l\'exercice';
        }
      });
    }
  }

  getExerciceTypeLabel(type: string): string {
    switch (type) {
      case 'FILL_BLANK': return 'Texte à trous';
      case 'DRAG_DROP': return 'Glisser-déposer';
      default: return type;
    }
  }

  // Méthodes pour l'affichage de l'exercice
  getDraggableElements(): ExerciceElement[] {
    if (!this.exercice?.elements) return [];
    const draggableElements = this.exercice.elements.filter(e => e.typeElement === 'DRAGGABLE');
    
    // Mélanger seulement pour l'aperçu étudiant
    if (!this.authService.isFormateur()) {
      return this.shuffleElements([...draggableElements]);
    }
    
    return draggableElements;
  }

  // Méthode pour mélanger les éléments aléatoirement (algorithme Fisher-Yates)
  private shuffleElements<T>(array: T[]): T[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  getDropZoneElements(): ExerciceElement[] {
    if (!this.exercice?.elements) return [];
    return this.exercice.elements.filter(e => e.typeElement === 'DROP_ZONE');
  }

  getTextAndBlankElements(): ExerciceElement[] {
    if (!this.exercice?.elements) return [];
    return this.exercice.elements
      .filter(e => e.typeElement === 'TEXT' || e.typeElement === 'BLANK')
      .sort((a, b) => a.positionOrdre - b.positionOrdre);
  }
}
