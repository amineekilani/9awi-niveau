import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Lecon {
  id?: number;
  titre: string;
  typeContenu: string; // 'TEXTE', 'PDF', 'IMAGE', 'VIDEO'
  contenuTexte?: string;
  fichierUrl?: string;
  ordre?: number;
  duree?: number;
  createdAt?: number;
  updatedAt?: number;
  moduleId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class LeconService {
  private apiUrl = `${environment.apiUrl}/lecons`;

  constructor(private http: HttpClient) { }

  createLecon(moduleId: number, lecon: Lecon): Observable<Lecon> {
    return this.http.post<Lecon>(`${this.apiUrl}/module/${moduleId}`, lecon);
  }

  createLeconWithFile(moduleId: number, formData: FormData): Observable<Lecon> {
    return this.http.post<Lecon>(`${this.apiUrl}/module/${moduleId}/with-file`, formData);
  }

  updateLecon(id: number, lecon: Lecon): Observable<Lecon> {
    return this.http.put<Lecon>(`${this.apiUrl}/${id}`, lecon);
  }

  updateLeconFile(id: number, file: File): Observable<Lecon> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<Lecon>(`${this.apiUrl}/${id}/file`, formData);
  }

  deleteLecon(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getLeconsByModule(moduleId: number): Observable<Lecon[]> {
    return this.http.get<Lecon[]>(`${this.apiUrl}/module/${moduleId}`);
  }

  getLeconById(id: number): Observable<Lecon> {
    return this.http.get<Lecon>(`${this.apiUrl}/${id}`);
  }

  getFileUrl(filename: string, typeContenu: string): string {
    // Utiliser l'API pour servir tous les fichiers avec les bons headers
    return `http://localhost:8080/api/files/lecons/${filename}`;
  }
}
