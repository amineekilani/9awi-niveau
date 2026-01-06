import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class LevelTestService {
  private apiUrl = 'http://localhost:8080/api/test/level';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Force la vérification des niveaux
   */
  forceCheckLevel(): Observable<any> {
    return this.http.post(`${this.apiUrl}/force-check`, {}, { headers: this.getHeaders() });
  }

  /**
   * Ajoute des XP de test
   */
  addTestXP(amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/add-xp/${amount}`, {}, { headers: this.getHeaders() });
  }
}