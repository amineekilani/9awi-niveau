import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ModuleProgress {
  id: number;
  titre: string;
  contenu: string;
  ordre: number;
  createdAt: number;
  updatedAt: number;
  coursId: number;
  totalLecons: number;
  leconsCompletees: number;
  progressionLecons: number;
  hasQuiz: boolean;
  quizId: number | null;
  quizTitre: string | null;
  quizPassed: boolean;
  bestScore: number | null;
  totalAttempts: number;
}

@Injectable({
  providedIn: 'root'
})
export class ModuleProgressService {
  private apiUrl = `${environment.apiUrl}/module-progress`;

  constructor(private http: HttpClient) { }

  getModulesWithProgress(coursId: number): Observable<ModuleProgress[]> {
    return this.http.get<ModuleProgress[]>(`${this.apiUrl}/cours/${coursId}`);
  }
}
