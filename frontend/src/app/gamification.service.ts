import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface BadgeResponse {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
  criteriaType: string;
  criteriaValue: number;
  isActive: boolean;
  createdAt: number;
  updatedAt: number;
  usersCount: number;
}

export interface BadgeRequest {
  name: string;
  description: string;
  iconUrl: string;
  criteriaType: string;
  criteriaValue: number;
  isActive: boolean;
}

export interface ChallengeResponse {
  id: number;
  name: string;
  description: string;
  challengeType: string;
  targetValue: number;
  xpReward: number;
  startDate: number;
  endDate: number;
  isActive: boolean;
  createdAt: number;
  updatedAt: number;
  participantsCount: number;
  completedCount: number;
}

export interface ChallengeRequest {
  name: string;
  description: string;
  challengeType: string;
  targetValue: number;
  xpReward: number;
  startDate: number;
  endDate: number;
  isActive: boolean;
}

export interface GamificationStatsResponse {
  totalBadges: number;
  activeBadges: number;
  totalChallenges: number;
  activeChallenges: number;
  totalXPAwarded: number;
  averageUserXP: number;
  totalBadgesEarned: number;
  totalChallengesCompleted: number;
}

export interface LeaderboardEntry {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  totalXP: number;
  currentLevel: number;
  levelName: string;
  badgesCount: number;
  rank: number;
}

export interface LeaderboardResponse {
  entries: LeaderboardEntry[];
}

export interface LevelResponse {
  id: number;
  level: number;
  xpRequired: number;
  name: string;
  description: string;
  createdAt: number;
}

export interface LevelRequest {
  level: number;
  xpRequired: number;
  name: string;
  description: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class GamificationService {
  private baseUrl = 'http://localhost:8080/api/admin/gamification'; // URL directe temporaire

  constructor(private http: HttpClient) {}

  // Statistiques
  getGamificationStats(): Observable<GamificationStatsResponse> {
    return this.http.get<GamificationStatsResponse>(`${this.baseUrl}/stats`).pipe(
      catchError(error => {
        console.error('Error in getGamificationStats:', error);
        return throwError(() => error);
      })
    );
  }

  // Badges
  getAllBadges(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<PageResponse<BadgeResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    return this.http.get<any>(`${this.baseUrl}/badges`, { params }).pipe(
      map((response: any) => {
        console.log('Raw response from backend:', response);
        return {
          ...response,
          content: response.content.map((badge: any) => {
            console.log(`Badge ${badge.name}: raw isActive = ${badge.isActive} (${typeof badge.isActive})`);
            return {
              ...badge,
              isActive: badge.isActive === true || badge.isActive === 'true' || badge.isActive === 1
            };
          })
        };
      }),
      catchError(error => {
        console.error('Error in getAllBadges:', error);
        return throwError(() => error);
      })
    );
  }

  getActiveBadges(): Observable<BadgeResponse[]> {
    return this.http.get<BadgeResponse[]>(`${this.baseUrl}/badges/active`);
  }

  getBadgeById(id: number): Observable<BadgeResponse> {
    return this.http.get<BadgeResponse>(`${this.baseUrl}/badges/${id}`);
  }

  createBadge(badge: BadgeRequest): Observable<BadgeResponse> {
    return this.http.post<BadgeResponse>(`${this.baseUrl}/badges`, badge);
  }

  updateBadge(id: number, badge: BadgeRequest): Observable<BadgeResponse> {
    return this.http.put<BadgeResponse>(`${this.baseUrl}/badges/${id}`, badge);
  }

  deleteBadge(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/badges/${id}`);
  }

  toggleBadgeStatus(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/badges/${id}/toggle-status`, {});
  }

  // Défis
  getAllChallenges(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<PageResponse<ChallengeResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    return this.http.get<PageResponse<ChallengeResponse>>(`${this.baseUrl}/challenges`, { params });
  }

  getActiveChallenges(): Observable<ChallengeResponse[]> {
    return this.http.get<ChallengeResponse[]>(`${this.baseUrl}/challenges/active`);
  }

  getChallengeById(id: number): Observable<ChallengeResponse> {
    return this.http.get<ChallengeResponse>(`${this.baseUrl}/challenges/${id}`);
  }

  createChallenge(challenge: ChallengeRequest): Observable<ChallengeResponse> {
    return this.http.post<ChallengeResponse>(`${this.baseUrl}/challenges`, challenge);
  }

  updateChallenge(id: number, challenge: ChallengeRequest): Observable<ChallengeResponse> {
    return this.http.put<ChallengeResponse>(`${this.baseUrl}/challenges/${id}`, challenge);
  }

  deleteChallenge(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/challenges/${id}`);
  }

  toggleChallengeStatus(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/challenges/${id}/toggle-status`, {});
  }

  // Classements
  getLeaderboard(page: number = 0, size: number = 20): Observable<LeaderboardResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<LeaderboardResponse>(`${this.baseUrl}/leaderboard`, { params });
  }

  getTopLeaderboard(limit: number = 10): Observable<LeaderboardResponse> {
    return this.http.get<LeaderboardResponse>(`${this.baseUrl}/leaderboard/top/${limit}`);
  }

  exportLeaderboard(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/leaderboard/export`, { 
      responseType: 'blob',
      headers: { 'Accept': 'text/csv' }
    });
  }

  // Niveaux
  getAllLevels(page: number = 0, size: number = 10, sortBy: string = 'level', sortDir: string = 'asc'): Observable<PageResponse<LevelResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    return this.http.get<PageResponse<LevelResponse>>(`${this.baseUrl}/levels`, { params });
  }

  getAllLevelsOrdered(): Observable<LevelResponse[]> {
    return this.http.get<LevelResponse[]>(`${this.baseUrl}/levels/all`);
  }

  getLevelById(id: number): Observable<LevelResponse> {
    return this.http.get<LevelResponse>(`${this.baseUrl}/levels/${id}`);
  }

  createLevel(level: LevelRequest): Observable<LevelResponse> {
    return this.http.post<LevelResponse>(`${this.baseUrl}/levels`, level);
  }

  updateLevel(id: number, level: LevelRequest): Observable<LevelResponse> {
    return this.http.put<LevelResponse>(`${this.baseUrl}/levels/${id}`, level);
  }

  deleteLevel(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/levels/${id}`);
  }
}