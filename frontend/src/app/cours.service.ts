import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Cours {
  id?: number;
  titre: string;
  description: string;
  createdAt?: number;
  updatedAt?: number;
  archived?: boolean;
  archivedAt?: number;
  formateurId?: number;
  formateurNom?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CoursService {
  private apiUrl = `${environment.apiUrl}/cours`;

  constructor(private http: HttpClient) { }

  createCours(cours: Cours): Observable<Cours> {
    return this.http.post<Cours>(this.apiUrl, cours);
  }

  updateCours(id: number, cours: Cours): Observable<Cours> {
    return this.http.put<Cours>(`${this.apiUrl}/${id}`, cours);
  }

  archiveCours(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/archive`, {});
  }

  unarchiveCours(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/unarchive`, {});
  }

  getMesCours(): Observable<Cours[]> {
    return this.http.get<Cours[]>(`${this.apiUrl}/mes-cours`);
  }

  getAllCours(): Observable<Cours[]> {
    return this.http.get<Cours[]>(this.apiUrl);
  }

  getCoursById(id: number): Observable<Cours> {
    return this.http.get<Cours>(`${this.apiUrl}/${id}`);
  }
}
