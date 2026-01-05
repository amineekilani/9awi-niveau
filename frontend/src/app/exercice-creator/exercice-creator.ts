import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ExerciceService, Exercice, ExerciceElement } from '../exercice.service';

@Component({
  selector: 'app-exercice-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './exercice-creator.html',
  styleUrls: ['./exercice-creator.css']
})
export class ExerciceCreatorComponent implements OnInit {
  moduleId!: number;
  exercice: Exercice = {
    titre: '',
    description: '',
    typeExercice: 'FILL_BLANK',
    elements: []
  };

  // Pour le texte à trous
  fillBlankText = '';
  
  // Pour drag and drop
  draggableItems: string[] = [''];
  dropZones: { label: string; correctAnswer: string }[] = [{ label: '', correctAnswer: '' }];
  
  // Pour l'appariement
  matchPairs: { question: string; answer: string; options: string[] }[] = [
    { question: '', answer: '', options: ['', '', '', ''] }
  ];

  loading = false;
  error = '';
  success = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private exerciceService: ExerciceService
  ) { }

  ngOnInit() {
    this.moduleId = Number(this.route.snapshot.paramMap.get('moduleId'));
  }

  // Méthodes pour texte à trous
  generateFillBlankElements() {
    const elements: ExerciceElement[] = [];
    let position = 1;
    
    // Parser le texte pour identifier les blancs marqués par [BLANK]
    const parts = this.fillBlankText.split(/(\[BLANK:([^\]]+)\])/);
    
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      
      if (part.startsWith('[BLANK:')) {
        // Extraire la réponse correcte
        const match = part.match(/\[BLANK:([^\]]+)\]/);
        if (match) {
          elements.push({
            contenu: '',
            typeElement: 'BLANK',
            positionOrdre: position++,
            reponseCorrecte: match[1].trim()
          });
        }
      } else if (part && !part.startsWith('[BLANK')) {
        // Texte normal
        elements.push({
          contenu: part,
          typeElement: 'TEXT',
          positionOrdre: position++
        });
      }
    }
    
    this.exercice.elements = elements;
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

  generateDragDropElements() {
    const elements: ExerciceElement[] = [];
    let position = 1;

    // Ajouter les éléments déplaçables
    this.draggableItems.forEach(item => {
      if (item.trim()) {
        elements.push({
          contenu: item.trim(),
          typeElement: 'DRAGGABLE',
          positionOrdre: position++
        });
      }
    });

    // Ajouter les zones de dépôt
    this.dropZones.forEach(zone => {
      if (zone.label.trim()) {
        elements.push({
          contenu: zone.label.trim(),
          typeElement: 'DROP_ZONE',
          positionOrdre: position++,
          reponseCorrecte: zone.correctAnswer.trim()
        });
      }
    });

    this.exercice.elements = elements;
  }

  // Méthodes pour l'appariement
  addMatchPair() {
    this.matchPairs.push({ question: '', answer: '', options: ['', '', '', ''] });
  }

  removeMatchPair(index: number) {
    this.matchPairs.splice(index, 1);
  }

  generateMatchingElements() {
    const elements: ExerciceElement[] = [];
    let position = 1;

    this.matchPairs.forEach(pair => {
      if (pair.question.trim() && pair.answer.trim()) {
        // Filtrer les options vides et s'assurer que la bonne réponse est incluse
        const validOptions = pair.options.filter(opt => opt.trim() !== '');
        if (!validOptions.includes(pair.answer)) {
          validOptions.push(pair.answer);
        }

        elements.push({
          contenu: pair.question.trim(),
          typeElement: 'MATCH_ITEM',
          positionOrdre: position++,
          reponseCorrecte: pair.answer.trim(),
          options: validOptions
        });
      }
    });

    this.exercice.elements = elements;
  }

  onTypeChange() {
    // Réinitialiser les éléments quand le type change
    this.exercice.elements = [];
    this.fillBlankText = '';
    this.draggableItems = [''];
    this.dropZones = [{ label: '', correctAnswer: '' }];
    this.matchPairs = [{ question: '', answer: '', options: ['', '', '', ''] }];
  }

  saveExercice() {
    if (!this.exercice.titre.trim()) {
      this.error = 'Le titre est obligatoire';
      return;
    }

    // Générer les éléments selon le type
    switch (this.exercice.typeExercice) {
      case 'FILL_BLANK':
        this.generateFillBlankElements();
        break;
      case 'DRAG_DROP':
        this.generateDragDropElements();
        break;
      case 'MATCHING':
        this.generateMatchingElements();
        break;
    }

    if (!this.exercice.elements || this.exercice.elements.length === 0) {
      this.error = 'L\'exercice doit contenir au moins un élément';
      return;
    }

    this.loading = true;
    this.error = '';

    this.exerciceService.createExercice(this.moduleId, this.exercice).subscribe({
      next: (response) => {
        this.success = 'Exercice créé avec succès !';
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/module', this.moduleId]);
        }, 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la création de l\'exercice';
        this.loading = false;
      }
    });
  }

  goBack() {
    this.router.navigate(['/module', this.moduleId]);
  }
}