import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QuizResultatDetail {
  quizId: number;
  titreQuiz: string;
  meilleurScore: number;
  nombreTentatives: number;
  dateDerniereTentative: string; // LocalDateTime du backend
  reussi: boolean;
}

export interface ModuleProgressionDetail {
  moduleId: number;
  titreModule: string;
  ordreModule: number;
  progressionPourcentage: number;
  totalLecons: number;
  leconsCompletees: number;
  quizDetail: QuizResultatDetail | null;
}

export interface ApprenantProgression {
  userId: number;
  nom: string;
  prenom: string;
  email: string;
  progressionPourcentage: number; // Renommé de progressionGlobale
  totalEtapes: number; // Renommé de totalLecons
  etapeCourante: number; // Renommé de leconsCompletees
  dateInscription: string; // LocalDateTime du backend
  dateCompletion?: string; // LocalDateTime du backend (optionnel)
  pointsGagnes: number;
  isCompleted: boolean;
  certificatGenere: boolean;
  statut: string;
  
  // Propriétés calculées pour compatibilité avec le template
  get progressionGlobale(): number;
  get totalLecons(): number;
  get leconsCompletees(): number;
  get enrolledAt(): number;
  get lastAccessedAt(): number;
  get modulesProgression(): ModuleProgressionDetail[];
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
