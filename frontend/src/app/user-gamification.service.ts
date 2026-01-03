import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserGamificationStats {
  totalPoints: number;
  currentLevel: number;
  levelName: string;
  levelDescription: string;
  pointsToNextLevel: number;
  nextLevelPoints: number;
  progressPercent: number;
  badgesCount: number;
  completedChallenges: number;
  leaderboardPosition: number;
  recentActivities: RecentActivity[];
  recentBadges: UserBadge[];
}

export interface RecentActivity {
  type: string;
  description: string;
  points: number;
  timeAgo: string;
  icon: string;
}

export interface UserBadge {
  id: number;
  name: string;
  description: string;
  criteriaType?: string;
  iconUrl: string;
  earnedAt: number;
  isNew: boolean;
}

export interface UserChallenge {
  id: number;
  name: string;
  description: string;
  challengeType: string;
  targetValue: number;
  currentProgress: number;
  progressPercent: number;
  xpReward: number;
  isCompleted: boolean;
  completedAt?: number;
  joinedAt: number;
  endDate?: number;
  timeRemaining?: string;
  isNew: boolean;
  isActive: boolean;
}

export interface UserLeaderboard {
  userPosition: {
    rank: number;
    name: string;
    totalPoints: number;
    level: number;
    levelName: string;
    badgesCount: number;
    isCurrentUser: boolean;
  };
  topLeaderboard: {
    rank: number;
    name: string;
    totalPoints: number;
    level: number;
    levelName: string;
    badgesCount: number;
    isCurrentUser: boolean;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class UserGamificationService {
  private apiUrl = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient) { }

  getUserStats(): Observable<UserGamificationStats> {
    return this.http.get<UserGamificationStats>(`${this.apiUrl}/stats`);
  }

  getUserBadges(filter: string = 'all'): Observable<UserBadge[]> {
    return this.http.get<UserBadge[]>(`${this.apiUrl}/badges?filter=${filter}`);
  }

  getUserChallenges(): Observable<UserChallenge[]> {
    return this.http.get<UserChallenge[]>(`${this.apiUrl}/challenges`);
  }

  getUserLeaderboard(): Observable<UserLeaderboard> {
    return this.http.get<UserLeaderboard>(`${this.apiUrl}/leaderboard`);
  }

  getRecentActivity(limit: number = 10): Observable<RecentActivity[]> {
    return this.http.get<RecentActivity[]>(`${this.apiUrl}/recent-activity?limit=${limit}`);
  }

  markBadgeAsViewed(badgeId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/badges/${badgeId}/view`, {});
  }

  markChallengeAsViewed(challengeId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/challenges/${challengeId}/view`, {});
  }
}