import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface ParcoursNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  xpEarned?: number;
  certificateReady: boolean;
  certificateUrl?: string;
  isRead: boolean;
  createdAt: string;
  parcoursId: number;
  parcoursTitle: string;
  parcoursDescription: string;
}

@Injectable({
  providedIn: 'root'
})
export class ParcoursNotificationService {
  private apiUrl = 'http://localhost:8080/api/parcours-notifications';

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
   * Obtenir toutes les notifications de l'utilisateur
   */
  getUserNotifications(): Observable<ParcoursNotification[]> {
    return this.http.get<ParcoursNotification[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  /**
   * Obtenir les notifications non lues
   */
  getUnreadNotifications(): Observable<ParcoursNotification[]> {
    return this.http.get<ParcoursNotification[]>(`${this.apiUrl}/unread`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir le nombre de notifications non lues
   */
  getUnreadNotificationsCount(): Observable<{ count: number }> {
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