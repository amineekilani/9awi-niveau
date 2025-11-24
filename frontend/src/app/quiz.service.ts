import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Question {
  id?: number;
  question: string;
  options: string[];
  correctAnswer: string;
  ordre?: number;
  createdAt?: number;
}

export interface Quiz {
  id?: number;
  titre: string;
  description?: string;
  moduleId?: number;
  questions?: Question[];
  createdAt?: number;
  updatedAt?: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  private apiUrl = `${environment.apiUrl}/quiz`;

  constructor(private http: HttpClient) { }

  createQuiz(moduleId: number, quiz: Quiz): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/module/${moduleId}`, quiz);
  }

  updateQuiz(quizId: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.apiUrl}/${quizId}`, quiz);
  }

  deleteQuiz(quizId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${quizId}`);
  }

  getQuizByModuleId(moduleId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/module/${moduleId}`);
  }

  getQuizById(quizId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${quizId}`);
  }

  addQuestion(quizId: number, question: Question): Observable<Question> {
    return this.http.post<Question>(`${this.apiUrl}/${quizId}/question`, question);
  }

  updateQuestion(questionId: number, question: Question): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/question/${questionId}`, question);
  }

  deleteQuestion(questionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/question/${questionId}`);
  }
}
