import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QuizResultatDetail {
  quizId: number;
  quizTitre: string;
  meilleurScore: number;
  nombreTentatives: number;
  derniereTentative: number;
  passed: boolean;
}

export interface ModuleProgressionDetail {
  moduleId: number;
  moduleTitre: string;
  totalLecons: number;
  leconsCompletees: number;
  progression: number;
  quizResultat: QuizResultatDetail | null;
}

export interface ApprenantProgression {
  userId: number;
  nom: string;
  prenom: string;
  email: string;
  profileImage: string | null;
  progressionGlobale: number;
  totalLecons: number;
  leconsCompletees: number;
  enrolledAt: number;
  lastAccessedAt: number;
  modulesProgression: ModuleProgressionDetail[];
  quizResultats: QuizResultatDetail[];
}

@Injectable({
  providedIn: 'root'
})
export class ApprenantProgressionService {
  private apiUrl = 'http://localhost:8080/api/enrollments';

  constructor(private http: HttpClient) {}

  getApprenantsProgression(coursId: number): Observable<ApprenantProgression[]> {
    return this.http.get<ApprenantProgression[]>(`${this.apiUrl}/cours/${coursId}/apprenants`);
  }
}
