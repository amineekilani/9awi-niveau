import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz, Question } from '../quiz.service';
import { QuizResultatService, QuizSubmission, ResultatQuiz, QuizAttempt } from '../quiz-resultat.service';

@Component({
  selector: 'app-quiz-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quiz-viewer.html',
  styleUrls: ['./quiz-viewer.css']
})
export class QuizViewerComponent implements OnInit {
  quizId!: number;
  moduleId!: number;
  quiz: Quiz | null = null;
  questions: Question[] = [];
  
  // État du quiz
  quizStarted = false;
  quizFinished = false;
  currentQuestionIndex = 0;
  reponses: { [questionId: number]: string } = {};
  startTime: number = 0;
  
  // Résultat
  resultat: ResultatQuiz | null = null;
  previousAttempts: QuizAttempt[] = [];
  bestScore: QuizAttempt | null = null;
  
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private quizResultatService: QuizResultatService
  ) {}

  ngOnInit() {
    this.quizId = Number(this.route.snapshot.paramMap.get('quizId'));
    this.moduleId = Number(this.route.snapshot.paramMap.get('moduleId'));
    this.loadQuiz();
    this.loadPreviousAttempts();
  }

  loadQuiz() {
    this.loading = true;
    this.quizService.getQuizById(this.quizId).subscribe({
      next: (data) => {
        this.quiz = data;
        this.questions = data.questions || [];
        console.log('Quiz chargé:', data);
        console.log('Questions:', this.questions);
        if (this.questions.length > 0) {
          console.log('Première question:', this.questions[0]);
          console.log('Options de la première question:', this.questions[0].options);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du quiz';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadPreviousAttempts() {
    this.quizResultatService.getUserQuizAttempts(this.quizId).subscribe({
      next: (attempts) => {
        this.previousAttempts = attempts;
      },
      error: (err) => {
        console.error('Erreur chargement tentatives:', err);
      }
    });

    this.quizResultatService.getBestScore(this.quizId).subscribe({
      next: (best) => {
        this.bestScore = best;
      },
      error: (err) => {
        console.error('Erreur chargement meilleur score:', err);
      }
    });
  }

  startQuiz() {
    this.quizStarted = true;
    this.quizFinished = false;
    this.currentQuestionIndex = 0;
    this.reponses = {};
    this.resultat = null;
    this.startTime = Date.now();
  }

  selectReponse(questionId: number, reponse: string) {
    this.reponses[questionId] = reponse;
  }

  nextQuestion() {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  previousQuestion() {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  canSubmit(): boolean {
    return this.questions.every(q => this.reponses[q.id!] !== undefined);
  }

  submitQuiz() {
    if (!this.canSubmit()) {
      alert('Veuillez répondre à toutes les questions');
      return;
    }

    const tempsPasse = Math.floor((Date.now() - this.startTime) / 1000);
    
    const submission: QuizSubmission = {
      reponses: this.reponses,
      tempsPasse: tempsPasse
    };

    this.loading = true;
    this.quizResultatService.submitQuiz(this.quizId, submission).subscribe({
      next: (resultat) => {
        this.resultat = resultat;
        this.quizFinished = true;
        this.loading = false;
        this.loadPreviousAttempts(); // Recharger les tentatives
      },
      error: (err) => {
        this.error = 'Erreur lors de la soumission du quiz';
        this.loading = false;
      }
    });
  }

  retryQuiz() {
    this.startQuiz();
  }

  goBack() {
    this.router.navigate(['/module', this.moduleId]);
  }

  getCurrentQuestion(): Question | null {
    return this.questions[this.currentQuestionIndex] || null;
  }

  getProgress(): number {
    if (this.questions.length === 0) return 0;
    return ((this.currentQuestionIndex + 1) / this.questions.length) * 100;
  }

  getAnsweredCount(): number {
    return Object.keys(this.reponses).length;
  }

  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  formatDate(timestamp: number): string {
    const date = new Date(timestamp);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getScoreColor(score: number): string {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBgColor(score: number): string {
    if (score >= 80) return 'bg-green-100';
    if (score >= 60) return 'bg-yellow-100';
    return 'bg-red-100';
  }
}
