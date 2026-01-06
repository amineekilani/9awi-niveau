import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface BadgeNotification {
  id: number;
  user: any;
  badge: {
    id: number;
    name: string;
    description: string;
    iconUrl: string;
    criteriaType: string;
  };
  isRead: boolean;
  isNew: boolean;
  createdAt: number;
}

@Injectable({
  providedIn: 'root'
})
export class BadgeNotificationService {
  private apiUrl = 'http://localhost:8080/api/badge-notifications';

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
  getUnreadBadgeNotifications(): Observable<BadgeNotification[]> {
    return this.http.get<BadgeNotification[]>(`${this.apiUrl}/unread`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir le nombre de notifications non lues
   */
  getUnreadBadgeNotificationsCount(): Observable<{ count: number }> {
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