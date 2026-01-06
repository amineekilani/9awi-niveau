import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface ChallengeNotification {
  id: number;
  user: any;
  challenge: {
    id: number;
    name: string;
    description: string;
    xpReward: number;
  };
  xpEarned: number;
  isRead: boolean;
  isNew: boolean;
  createdAt: number;
}

@Injectable({
  providedIn: 'root'
})
export class ChallengeNotificationService {
  private apiUrl = 'http://localhost:8080/api/challenge-notifications';

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
   * Obtenir les notifications non lues
   */
  getUnreadChallengeNotifications(): Observable<ChallengeNotification[]> {
    return this.http.get<ChallengeNotification[]>(`${this.apiUrl}/unread`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir le nombre de notifications non lues
   */
  getUnreadChallengeNotificationsCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread/count`, { headers: this.getHeaders() });
  }

  /**
   * Marquer une notification comme lue
   */
  markNotificationAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {}, { headers: this.getHeaders() });
  }

  /**
   * Marquer toutes les notifications comme lues
   */
  markAllNotificationsAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {}, { headers: this.getHeaders() });
  }
}