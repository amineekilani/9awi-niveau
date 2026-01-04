import { Injectable } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ParcoursEtapeResponse, ParcoursResponse, TypeParcours } from './parcours.service';
import { EnrollmentService } from './enrollment.service';
import { QuizResultatService } from './quiz-resultat.service';

export interface EtapeValidation {
  etapeId: number;
  isDebloque: boolean;
  isComplete: boolean;
  progressionCours: number;
  scoreObtenu: number;
  conditionsRemplies: {
    scoreMinimum: boolean;
    pourcentageCompletion: boolean;
    quizObligatoires: boolean;
  };
  raisonVerrouillage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ParcoursValidationService {

  constructor(
    private enrollmentService: EnrollmentService,
    private quizResultatService: QuizResultatService
  ) {}

  /**
   * Valide toutes les étapes d'un parcours pour un utilisateur
   * Maintenant que le backend gère la validation, on simplifie cette méthode
   */
  validerEtapesParcours(parcours: ParcoursResponse, etapes: ParcoursEtapeResponse[]): Observable<EtapeValidation[]> {
    // Le backend fournit déjà les données de validation dans les étapes
    // On convertit simplement les données reçues
    const validations: EtapeValidation[] = etapes.map(etape => ({
      etapeId: etape.id,
      isDebloque: etape.isDebloque || false,
      isComplete: etape.isComplete || false,
      progressionCours: etape.progressionCours || 0,
      scoreObtenu: etape.scoreObtenu || 0,
      conditionsRemplies: {
        scoreMinimum: this.verifierScoreMinimum(etape, etape.scoreObtenu || 0),
        pourcentageCompletion: this.verifierPourcentageCompletion(etape, etape.progressionCours || 0),
        quizObligatoires: this.verifierQuizObligatoires(etape, etape.scoreObtenu || 0)
      },
      raisonVerrouillage: this.genererRaisonVerrouillage(etape, etapes)
    }));

    return of(validations);
  }

  /**
   * Vérifier le score minimum
   */
  private verifierScoreMinimum(etape: ParcoursEtapeResponse, scoreObtenu: number): boolean {
    if (etape.scoreMinimum === 0) return true; // Pas de score minimum requis
    return scoreObtenu >= etape.scoreMinimum;
  }

  /**
   * Vérifier le pourcentage de completion
   */
  private verifierPourcentageCompletion(etape: ParcoursEtapeResponse, progressionCours: number): boolean {
    if (etape.pourcentageCompletionRequis === 0) return true; // Pas de pourcentage requis
    return progressionCours >= etape.pourcentageCompletionRequis;
  }

  /**
   * Vérifier les quiz obligatoires (version simplifiée)
   */
  private verifierQuizObligatoires(etape: ParcoursEtapeResponse, scoreObtenu: number): boolean {
    if (!etape.quizObligatoires) return true; // Pas de quiz obligatoires
    return scoreObtenu >= 60; // Considérer réussi si score >= 60%
  }

  /**
   * Générer la raison du verrouillage (simplifiée)
   */
  private genererRaisonVerrouillage(etape: ParcoursEtapeResponse, etapes: ParcoursEtapeResponse[]): string | undefined {
    if (etape.isDebloque) return undefined;

    const raisons = [];

    if (etape.ordreEtape > 1) {
      const etapePrecedente = etapes.find(e => e.ordreEtape === etape.ordreEtape - 1);
      if (etapePrecedente && !etapePrecedente.isComplete) {
        raisons.push("Terminez l'étape précédente");
      }
    }

    if (etape.scoreMinimum > 0 && (etape.scoreObtenu || 0) < etape.scoreMinimum) {
      raisons.push(`Score minimum requis: ${etape.scoreMinimum}%`);
    }

    if (etape.pourcentageCompletionRequis > 0 && (etape.progressionCours || 0) < etape.pourcentageCompletionRequis) {
      raisons.push(`Completion requise: ${etape.pourcentageCompletionRequis}%`);
    }

    if (etape.quizObligatoires && (etape.scoreObtenu || 0) < 60) {
      raisons.push("Quiz obligatoires non réussis");
    }

    return raisons.length > 0 ? raisons.join(', ') : "Étape verrouillée";
  }

  /**
   * Créer une étape verrouillée par défaut
   */
  private creerEtapeVerrouillee(etape: ParcoursEtapeResponse, raison = "Étape verrouillée"): EtapeValidation {
    return {
      etapeId: etape.id,
      isDebloque: false,
      isComplete: false,
      progressionCours: 0,
      scoreObtenu: 0,
      conditionsRemplies: {
        scoreMinimum: false,
        pourcentageCompletion: false,
        quizObligatoires: false
      },
      raisonVerrouillage: raison
    };
  }

  /**
   * Récupérer les résultats de quiz pour plusieurs cours
   */
  private getQuizResultsForCours(coursIds: number[]): Observable<any[]> {
    if (coursIds.length === 0) {
      return of([]);
    }

    // Récupérer les résultats de quiz pour chaque cours
    const quizRequests = coursIds.map(coursId => 
      this.quizResultatService.getBestScoreForCours(coursId).pipe(
        map(result => ({ coursId, ...result })),
        catchError(() => of({ coursId, score: 0, passed: false }))
      )
    );

    return forkJoin(quizRequests);
  }
}