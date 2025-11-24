import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ModuleService, Module } from '../module.service';
import { LeconService, Lecon } from '../lecon.service';
import { QuizService, Quiz, Question } from '../quiz.service';
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
  quiz: Quiz | null = null;
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

  // Quiz form
  showQuizForm = false;
  editingQuiz = false;
  quizForm: Quiz = {
    titre: '',
    description: '',
    questions: []
  };

  // Question form
  showQuestionForm = false;
  editingQuestion: Question | null = null;
  questionForm: Question = {
    question: '',
    options: ['', '', '', ''],
    correctAnswer: '',
    ordre: undefined
  };

  constructor(
    private moduleService: ModuleService,
    private leconService: LeconService,
    private quizService: QuizService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.moduleId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadModule();
    this.loadLecons();
    this.loadQuiz();
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

  // Quiz methods
  loadQuiz() {
    this.quizService.getQuizByModuleId(this.moduleId).subscribe({
      next: (data) => {
        console.log('Quiz chargé:', data);
        // Vérifier si c'est un vrai quiz ou un message
        if (data && data.id) {
          this.quiz = data;
          console.log('Quiz ID:', this.quiz.id);
        } else {
          this.quiz = null;
          console.log('Pas de quiz pour ce module');
        }
      },
      error: (err) => {
        // Pas de quiz pour ce module, c'est normal
        console.log('Erreur chargement quiz (normal si pas de quiz):', err);
        this.quiz = null;
      }
    });
  }

  openQuizForm() {
    if (this.quiz) {
      this.editingQuiz = true;
      this.quizForm = { ...this.quiz };
    } else {
      this.editingQuiz = false;
      this.quizForm = {
        titre: '',
        description: '',
        questions: []
      };
    }
    this.showQuizForm = true;
    this.error = '';
    this.success = '';
  }

  cancelQuizForm() {
    this.showQuizForm = false;
    this.editingQuiz = false;
  }

  saveQuiz() {
    this.error = '';
    this.success = '';

    if (this.editingQuiz && this.quiz) {
      this.quizService.updateQuiz(this.quiz.id!, this.quizForm).subscribe({
        next: (response) => {
          console.log('Quiz modifié:', response);
          this.success = 'Quiz modifié avec succès';
          this.loadQuiz();
          this.cancelQuizForm();
        },
        error: (err) => {
          console.error('Erreur modification quiz:', err);
          this.error = err.error?.message || 'Erreur lors de la modification du quiz';
        }
      });
    } else {
      this.quizService.createQuiz(this.moduleId, this.quizForm).subscribe({
        next: (response) => {
          console.log('Quiz créé:', response);
          this.success = 'Quiz créé avec succès';
          this.quiz = response; // Mettre à jour immédiatement
          this.loadQuiz(); // Recharger pour être sûr
          this.cancelQuizForm();
        },
        error: (err) => {
          console.error('Erreur création quiz:', err);
          this.error = err.error?.message || 'Erreur lors de la création du quiz';
        }
      });
    }
  }

  deleteQuiz() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce quiz et toutes ses questions ?')) {
      this.quizService.deleteQuiz(this.quiz!.id!).subscribe({
        next: () => {
          this.success = 'Quiz supprimé avec succès';
          this.quiz = null;
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la suppression du quiz';
        }
      });
    }
  }

  // Question methods
  openQuestionForm() {
    this.showQuestionForm = true;
    this.editingQuestion = null;
    this.questionForm = {
      question: '',
      options: ['', '', '', ''],
      correctAnswer: '',
      ordre: undefined
    };
    this.error = '';
    this.success = '';
  }

  editQuestion(question: Question) {
    this.showQuestionForm = true;
    this.editingQuestion = question;
    this.questionForm = { ...question, options: [...question.options] };
    this.error = '';
    this.success = '';
  }

  cancelQuestionForm() {
    this.showQuestionForm = false;
    this.editingQuestion = null;
  }

  addOption() {
    this.questionForm.options.push('');
  }

  removeOption(index: number) {
    if (this.questionForm.options.length > 2) {
      this.questionForm.options.splice(index, 1);
    }
  }

  saveQuestion() {
    this.error = '';
    this.success = '';

    console.log('=== Sauvegarde de question ===');
    console.log('Question form:', this.questionForm);
    console.log('Options:', this.questionForm.options);
    console.log('Réponse correcte:', this.questionForm.correctAnswer);

    // Valider que toutes les options sont remplies
    if (this.questionForm.options.some(opt => !opt.trim())) {
      this.error = 'Toutes les options doivent être remplies';
      return;
    }

    // Valider que la réponse correcte est dans les options
    if (!this.questionForm.options.includes(this.questionForm.correctAnswer)) {
      this.error = 'La réponse correcte doit être l\'une des options';
      return;
    }

    if (this.editingQuestion) {
      console.log('Modification de la question:', this.editingQuestion.id);
      this.quizService.updateQuestion(this.editingQuestion.id!, this.questionForm).subscribe({
        next: (response) => {
          console.log('Question modifiée:', response);
          this.success = 'Question modifiée avec succès';
          this.loadQuiz();
          this.cancelQuestionForm();
        },
        error: (err) => {
          console.error('Erreur modification:', err);
          this.error = err.error?.message || 'Erreur lors de la modification de la question';
        }
      });
    } else {
      // Vérifier que le quiz existe et a un ID
      if (!this.quiz || !this.quiz.id) {
        this.error = 'Erreur: Le quiz n\'est pas chargé correctement. Veuillez recharger la page.';
        console.error('Quiz non chargé ou sans ID:', this.quiz);
        return;
      }
      
      console.log('Ajout de la question au quiz:', this.quiz.id);
      this.quizService.addQuestion(this.quiz.id, this.questionForm).subscribe({
        next: (response) => {
          console.log('Question ajoutée:', response);
          this.success = 'Question ajoutée avec succès';
          this.loadQuiz();
          this.cancelQuestionForm();
        },
        error: (err) => {
          console.error('Erreur ajout:', err);
          this.error = err.error?.message || 'Erreur lors de l\'ajout de la question';
        }
      });
    }
  }

  deleteQuestion(questionId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette question ?')) {
      this.quizService.deleteQuestion(questionId).subscribe({
        next: () => {
          this.success = 'Question supprimée avec succès';
          this.loadQuiz();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors de la suppression de la question';
        }
      });
    }
  }

  trackByIndex(index: number): number {
    return index;
  }

  goBack() {
    this.router.navigate(['/cours', this.module?.coursId]);
  }
}
