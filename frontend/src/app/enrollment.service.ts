import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Enrollment {
  id: number;
  userId: number;
  coursId: number;
  coursTitle: string;
  coursDescription: string;
  enrolledAt: number;
  progress: number;
  lastAccessedAt: number;
  totalLecons: number;
  completedLecons: number;
  totalQuiz: number;
  completedQuiz: number;
  passedQuiz: number;
}

export interface EnrollmentRequest {
  coursId: number;
}

export interface LeconCompletionRequest {
  leconId: number;
}

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  private apiUrl = `${environment.apiUrl}/enrollments`;

  constructor(private http: HttpClient) { }

  enrollInCourse(coursId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.apiUrl, { coursId });
  }

  getUserEnrollments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(this.apiUrl);
  }

  getEnrollmentDetails(coursId: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/cours/${coursId}`);
  }

  markLeconAsCompleted(coursId: number, leconId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(
      `${this.apiUrl}/cours/${coursId}/complete-lecon`,
      { leconId }
    );
  }

  unmarkLeconAsCompleted(coursId: number, leconId: number): Observable<Enrollment> {
    return this.http.delete<Enrollment>(
      `${this.apiUrl}/cours/${coursId}/lecons/${leconId}/completion`
    );
  }

  getCompletedLeconIds(coursId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/cours/${coursId}/completed-lecons`);
  }

  isEnrolled(coursId: number): Observable<boolean> {
    return new Observable(observer => {
      this.getEnrollmentDetails(coursId).subscribe({
        next: () => {
          observer.next(true);
          observer.complete();
        },
        error: () => {
          observer.next(false);
          observer.complete();
        }
      });
    });
  }
}
