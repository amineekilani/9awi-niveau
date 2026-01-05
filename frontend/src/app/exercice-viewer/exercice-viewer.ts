import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ExerciceService, Exercice, ExerciceElement } from '../exercice.service';
import { ExerciceResultatService, ExerciceSubmission, ResultatExercice, ExerciceAttempt } from '../exercice-resultat.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-exercice-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './exercice-viewer.html',
  styleUrls: ['./exercice-viewer.css']
})
export class ExerciceViewerComponent implements OnInit {
  exerciceId!: number;
  moduleId!: number;
  exercice: Exercice | null = null;
  elements: ExerciceElement[] = [];

  // État de l'exercice
  exerciceStarted = false;
  exerciceFinished = false;
  reponses: { [elementId: number]: string } = {};
  startTime: number = 0;

  // Résultat
  resultat: ResultatExercice | null = null;
  previousAttempts: ExerciceAttempt[] = [];
  bestScore: ExerciceAttempt | null = null;

  // Drag and Drop
  draggedItem: string | null = null;
  draggedFromId: number | null = null;
  shuffledDraggableItems: string[] = [];

  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private exerciceService: ExerciceService,
    private exerciceResultatService: ExerciceResultatService,
    public authService: AuthService
  ) { }

  ngOnInit() {
    this.exerciceId = Number(this.route.snapshot.paramMap.get('exerciceId'));
    this.moduleId = Number(this.route.snapshot.paramMap.get('moduleId'));
    this.loadExercice();
    this.loadPreviousAttempts();
  }

  loadExercice() {
    this.loading = true;
    this.exerciceService.getExerciceById(this.exerciceId).subscribe({
      next: (data) => {
        this.exercice = data;
        this.elements = data.elements || [];
        console.log('Exercice chargé:', data);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement de l\'exercice';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadPreviousAttempts() {
    this.exerciceResultatService.getUserExerciceAttempts(this.exerciceId).subscribe({
      next: (attempts) => {
        this.previousAttempts = attempts;
      },
      error: (err) => {
        console.error('Erreur chargement tentatives:', err);
      }
    });

    this.exerciceResultatService.getBestScore(this.exerciceId).subscribe({
      next: (best) => {
        this.bestScore = best;
      },
      error: (err) => {
        console.error('Erreur chargement meilleur score:', err);
      }
    });
  }

  startExercice() {
    this.exerciceStarted = true;
    this.exerciceFinished = false;
    this.reponses = {};
    this.resultat = null;
    this.startTime = Date.now();
    // Remélanger les termes pour chaque nouvelle tentative
    this.shuffledDraggableItems = [];
  }

  // Méthodes pour texte à trous
  updateBlankResponse(elementId: number, value: string) {
    this.reponses[elementId] = value;
  }

  // Méthodes pour drag and drop
  onDragStart(event: DragEvent, item: string, fromElementId?: number) {
    this.draggedItem = item;
    this.draggedFromId = fromElementId || null;
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', item);
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  onDrop(event: DragEvent, targetElementId: number) {
    event.preventDefault();
    if (this.draggedItem) {
      // Si l'élément vient d'une autre zone, la vider
      if (this.draggedFromId && this.draggedFromId !== targetElementId) {
        this.reponses[this.draggedFromId] = '';
      }
      
      this.reponses[targetElementId] = this.draggedItem;
      this.draggedItem = null;
      this.draggedFromId = null;
    }
  }

  // Méthodes pour appariement
  selectMatchItem(elementId: number, option: string) {
    this.reponses[elementId] = option;
  }

  canSubmit(): boolean {
    const evaluableElements = this.elements.filter(e => 
      e.reponseCorrecte && e.reponseCorrecte.trim() !== ''
    );
    return evaluableElements.every(e => this.reponses[e.id!] !== undefined && this.reponses[e.id!] !== '');
  }

  submitExercice() {
    if (!this.canSubmit()) {
      alert('Veuillez compléter tous les éléments de l\'exercice');
      return;
    }

    const tempsPasse = Math.floor((Date.now() - this.startTime) / 1000);

    const submission: ExerciceSubmission = {
      reponses: this.reponses,
      tempsPasse: tempsPasse
    };

    this.loading = true;
    this.exerciceResultatService.submitExercice(this.exerciceId, submission).subscribe({
      next: (resultat) => {
        this.resultat = resultat;
        this.exerciceFinished = true;
        this.loading = false;
        this.loadPreviousAttempts();
      },
      error: (err) => {
        this.error = 'Erreur lors de la soumission de l\'exercice';
        this.loading = false;
      }
    });
  }

  retryExercice() {
    this.startExercice();
  }

  goBack() {
    this.router.navigate(['/module', this.moduleId]);
  }

  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
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

  getDraggableItems(): string[] {
    if (this.exercice?.typeExercice === 'DRAG_DROP') {
      // Si les termes n'ont pas encore été mélangés, les mélanger une seule fois
      if (this.shuffledDraggableItems.length === 0) {
        const items = this.elements
          .filter(e => e.typeElement === 'DRAGGABLE')
          .map(e => e.contenu);
        this.shuffledDraggableItems = this.shuffleArray([...items]);
      }
      return this.shuffledDraggableItems;
    }
    return [];
  }

  // Méthode pour mélanger un tableau aléatoirement (algorithme Fisher-Yates)
  private shuffleArray<T>(array: T[]): T[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  getDropZones(): ExerciceElement[] {
    if (this.exercice?.typeExercice === 'DRAG_DROP') {
      return this.elements.filter(e => e.typeElement === 'DROP_ZONE');
    }
    return [];
  }

  getTextElements(): ExerciceElement[] {
    return this.elements.filter(e => e.typeElement === 'TEXT');
  }

  getBlankElements(): ExerciceElement[] {
    return this.elements.filter(e => e.typeElement === 'BLANK');
  }
}