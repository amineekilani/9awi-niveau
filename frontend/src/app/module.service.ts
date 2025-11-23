import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Module {
  id?: number;
  titre: string;
  contenu: string;
  ordre?: number;
  createdAt?: number;
  updatedAt?: number;
  coursId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ModuleService {
  private apiUrl = `${environment.apiUrl}/modules`;

  constructor(private http: HttpClient) { }

  createModule(coursId: number, module: Module): Observable<Module> {
    return this.http.post<Module>(`${this.apiUrl}/cours/${coursId}`, module);
  }

  updateModule(id: number, module: Module): Observable<Module> {
    return this.http.put<Module>(`${this.apiUrl}/${id}`, module);
  }

  deleteModule(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getModulesByCours(coursId: number): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.apiUrl}/cours/${coursId}`);
  }

  getModuleById(id: number): Observable<Module> {
    return this.http.get<Module>(`${this.apiUrl}/${id}`);
  }
}
