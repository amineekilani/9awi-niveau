import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GamificationInitService {
  private baseUrl = 'http://localhost:8080/api/init'; // URL directe temporaire

  constructor(private http: HttpClient) {}

  initializeGamification(): Observable<any> {
    return this.http.post(`${this.baseUrl}/gamification`, {});
  }

  checkGamificationStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/gamification/status`);
  }
}