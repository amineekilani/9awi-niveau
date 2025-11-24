import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface QuizSubmission {
  reponses: { [questionId: number]: string };
  tempsPasse: number;
}

export interface QuestionResultat {
  questionId: number;
  question: string;
  reponseUtilisateur: string;
  reponseCorrecte: string;
  correct: boolean;
}

export interface ResultatQuiz {
  id: number;
  userId: number;
  quizId: number;
  quizTitre: string;
  score: number;
  datePassed: number;
  nombreQuestions: number;
  reponsesCorrectes: number;
  tempsPasse: number;
  details: QuestionResultat[];
}

export interface QuizAttempt {
  id: number;
  score: number;
  datePassed: number;
  reponsesCorrectes: number;
  nombreQuestions: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuizResultatService {
  private apiUrl = `${environment.apiUrl}/quiz-resultats`;

  constructor(private http: HttpClient) { }

  submitQuiz(quizId: number, submission: QuizSubmission): Observable<ResultatQuiz> {
    return this.http.post<ResultatQuiz>(`${this.apiUrl}/quiz/${quizId}/submit`, submission);
  }

  getUserQuizAttempts(quizId: number): Observable<QuizAttempt[]> {
    return this.http.get<QuizAttempt[]>(`${this.apiUrl}/quiz/${quizId}/attempts`);
  }

  getBestScore(quizId: number): Observable<QuizAttempt> {
    return this.http.get<QuizAttempt>(`${this.apiUrl}/quiz/${quizId}/best-score`);
  }

  getResultatDetails(resultatId: number): Observable<ResultatQuiz> {
    return this.http.get<ResultatQuiz>(`${this.apiUrl}/${resultatId}`);
  }
}
