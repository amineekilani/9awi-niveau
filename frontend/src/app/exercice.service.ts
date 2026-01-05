import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ExerciceElement {
  id?: number;
  contenu: string;
  typeElement: 'TEXT' | 'BLANK' | 'DRAGGABLE' | 'DROP_ZONE' | 'MATCH_ITEM';
  positionOrdre: number;
  reponseCorrecte?: string;
  options?: string[];
  createdAt?: number;
}

export interface Exercice {
  id?: number;
  titre: string;
  description?: string;
  typeExercice: 'FILL_BLANK' | 'DRAG_DROP' | 'MATCHING';
  moduleId?: number;
  elements?: ExerciceElement[];
  createdAt?: number;
  updatedAt?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ExerciceService {
  private apiUrl = `${environment.apiUrl}/exercice`;

  constructor(private http: HttpClient) { }

  createExercice(moduleId: number, exercice: Exercice): Observable<Exercice> {
    return this.http.post<Exercice>(`${this.apiUrl}/module/${moduleId}`, exercice);
  }

  updateExercice(exerciceId: number, exercice: Exercice): Observable<Exercice> {
    return this.http.put<Exercice>(`${this.apiUrl}/${exerciceId}`, exercice);
  }

  deleteExercice(exerciceId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${exerciceId}`);
  }

  getExerciceByModuleId(moduleId: number): Observable<Exercice> {
    return this.http.get<Exercice>(`${this.apiUrl}/module/${moduleId}`);
  }

  getExerciceById(exerciceId: number): Observable<Exercice> {
    return this.http.get<Exercice>(`${this.apiUrl}/${exerciceId}`);
  }

  addElement(exerciceId: number, element: ExerciceElement): Observable<ExerciceElement> {
    return this.http.post<ExerciceElement>(`${this.apiUrl}/${exerciceId}/element`, element);
  }

  updateElement(elementId: number, element: ExerciceElement): Observable<ExerciceElement> {
    return this.http.put<ExerciceElement>(`${this.apiUrl}/element/${elementId}`, element);
  }

  deleteElement(elementId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/element/${elementId}`);
  }
}