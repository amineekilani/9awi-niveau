import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TestBackendService {
  constructor(private http: HttpClient) {}

  testConnection(): Observable<any> {
    console.log('Testing connection to backend...');
    return this.http.get('http://localhost:8080/api/test').pipe( // URL directe temporaire
      tap(response => console.log('Backend test response:', response)),
      catchError(error => {
        console.error('Backend test error:', error);
        return throwError(() => error);
      })
    );
  }

  testGamificationInit(): Observable<any> {
    return this.http.post('http://localhost:8080/api/init/gamification', {}); // URL directe temporaire
  }
}