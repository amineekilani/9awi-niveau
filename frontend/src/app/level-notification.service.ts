import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface LevelNotification {
  id: number;
  oldLevel: number;
  newLevel: number;
  levelName: string;
  totalXP: number;
  xpGained: number;
  isRead: boolean;
  isNew: boolean;
  createdAt: number;
  timeAgo: string;
}

@Injectable({
  providedIn: 'root'
})
export class LevelNotificationService {
  private apiUrl = 'http://localhost:8080/api/level-notifications';

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
   * Obtenir toutes les notifications de niveau de l'utilisateur
   */
  getUserLevelNotifications(): Observable<LevelNotification[]> {
    return this.http.get<LevelNotification[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  /**
   * Obtenir les notifications non lues
   */
  getUnreadLevelNotifications(): Observable<LevelNotification[]> {
    return this.http.get<LevelNotification[]>(`${this.apiUrl}/unread`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir les nouvelles notifications (pour les popups)
   */
  getNewLevelNotifications(): Observable<LevelNotification[]> {
    return this.http.get<LevelNotification[]>(`${this.apiUrl}/new`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir le nombre de notifications non lues
   */
  getUnreadLevelNotificationsCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread/count`, { headers: this.getHeaders() });
  }

  /**
   * Obtenir le nombre de nouvelles notifications
   */
  getNewLevelNotificationsCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/new/count`, { headers: this.getHeaders() });
  }

  /**
   * Marquer une notification comme lue
   */
  markNotificationAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {}, { headers: this.getHeaders() });
  }

  /**
   * Marquer une notification comme vue (pour les popups)
   */
  markNotificationAsViewed(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/viewed`, {}, { headers: this.getHeaders() });
  }

  /**
   * Marquer toutes les notifications comme lues
   */
  markAllNotificationsAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {}, { headers: this.getHeaders() });
  }
}