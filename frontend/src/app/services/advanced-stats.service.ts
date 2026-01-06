import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, forkJoin } from 'rxjs';
import { AdminService } from '../admin.service';
import { GamificationService } from '../gamification.service';

export interface TimeSeriesData {
  date: string;
  value: number;
}

export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    borderWidth?: number;
    fill?: boolean;
  }[];
}

export interface AdvancedStatsResponse {
  userGrowth: TimeSeriesData[];
  roleDistribution: { role: string; count: number }[];
  activityTrend: TimeSeriesData[];
  xpDistribution: { range: string; count: number }[];
  badgeStats: { name: string; obtained: number; total: number }[];
  challengeCompletion: { completed: number; total: number };
  topPerformers: { name: string; xp: number; level: string }[];
  monthlyEngagement: TimeSeriesData[];
}

@Injectable({
  providedIn: 'root'
})
export class AdvancedStatsService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private adminService: AdminService,
    private gamificationService: GamificationService
  ) {}

  getAdvancedStats(): Observable<AdvancedStatsResponse> {
    // Simuler des données avancées en combinant les services existants
    return forkJoin({
      userStats: this.adminService.getUserStats(),
      gamificationStats: this.gamificationService.getGamificationStats(),
      leaderboard: this.gamificationService.getTopLeaderboard(10)
    }).pipe(
      map(({ userStats, gamificationStats, leaderboard }) => {
        return {
          userGrowth: this.generateUserGrowthData(userStats.totalUsers),
          roleDistribution: [
            { role: 'Étudiants', count: userStats.etudiantUsers },
            { role: 'Formateurs', count: userStats.formateurUsers },
            { role: 'Administrateurs', count: userStats.adminUsers }
          ],
          activityTrend: this.generateActivityTrendData(userStats.activeUsers),
          xpDistribution: this.generateXpDistributionData(gamificationStats.totalXPAwarded),
          badgeStats: this.generateBadgeStatsData(gamificationStats.totalBadges, gamificationStats.totalBadgesEarned),
          challengeCompletion: {
            completed: gamificationStats.totalChallengesCompleted,
            total: gamificationStats.totalChallenges
          },
          topPerformers: leaderboard.entries.slice(0, 5).map((user: any) => ({
            name: user.firstName + ' ' + user.lastName,
            xp: user.totalXP,
            level: user.levelName
          })),
          monthlyEngagement: this.generateMonthlyEngagementData()
        };
      })
    );
  }

  private generateUserGrowthData(totalUsers: number): TimeSeriesData[] {
    const data: TimeSeriesData[] = [];
    const now = new Date();
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      // Simuler une croissance progressive
      const baseGrowth = Math.floor(totalUsers * 0.7);
      const dailyGrowth = Math.floor((totalUsers - baseGrowth) * (30 - i) / 30);
      const randomVariation = Math.floor(Math.random() * 10) - 5;
      
      data.push({
        date: date.toISOString().split('T')[0],
        value: Math.max(0, baseGrowth + dailyGrowth + randomVariation)
      });
    }
    
    return data;
  }

  private generateActivityTrendData(activeUsers: number): TimeSeriesData[] {
    const data: TimeSeriesData[] = [];
    const now = new Date();
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      // Simuler des variations d'activité (plus élevée en semaine)
      const dayOfWeek = date.getDay();
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
      const baseActivity = isWeekend ? activeUsers * 0.6 : activeUsers;
      const randomVariation = Math.floor(Math.random() * (activeUsers * 0.2)) - (activeUsers * 0.1);
      
      data.push({
        date: date.toLocaleDateString('fr-FR', { weekday: 'short' }),
        value: Math.max(0, Math.floor(baseActivity + randomVariation))
      });
    }
    
    return data;
  }

  private generateXpDistributionData(totalXp: number): { range: string; count: number }[] {
    return [
      { range: '0-100', count: Math.floor(Math.random() * 50) + 20 },
      { range: '101-500', count: Math.floor(Math.random() * 40) + 30 },
      { range: '501-1000', count: Math.floor(Math.random() * 30) + 25 },
      { range: '1001-2000', count: Math.floor(Math.random() * 20) + 15 },
      { range: '2000+', count: Math.floor(Math.random() * 15) + 10 }
    ];
  }

  private generateBadgeStatsData(totalBadges: number, badgesObtained: number): { name: string; obtained: number; total: number }[] {
    const badgeTypes = ['Débutant', 'Intermédiaire', 'Avancé', 'Expert', 'Maître'];
    return badgeTypes.map(type => ({
      name: type,
      obtained: Math.floor(Math.random() * 20) + 5,
      total: Math.floor(Math.random() * 10) + 15
    }));
  }

  private generateMonthlyEngagementData(): TimeSeriesData[] {
    const data: TimeSeriesData[] = [];
    const months = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun'];
    
    months.forEach(month => {
      data.push({
        date: month,
        value: Math.floor(Math.random() * 100) + 50
      });
    });
    
    return data;
  }

  // Méthodes utilitaires pour convertir en format Chart.js
  convertToLineChartData(data: TimeSeriesData[], label: string, color: string): ChartData {
    return {
      labels: data.map(d => d.date),
      datasets: [{
        label,
        data: data.map(d => d.value),
        borderColor: color,
        backgroundColor: color + '20',
        borderWidth: 2,
        fill: true
      }]
    };
  }

  convertToPieChartData(data: { role: string; count: number }[]): ChartData {
    const colors = ['#063cdf', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'];
    
    return {
      labels: data.map(d => d.role),
      datasets: [{
        label: 'Distribution',
        data: data.map(d => d.count),
        backgroundColor: colors.slice(0, data.length)
      }]
    };
  }

  convertToBarChartData(data: { range: string; count: number }[], label: string, color: string): ChartData {
    return {
      labels: data.map(d => d.range),
      datasets: [{
        label,
        data: data.map(d => d.count),
        backgroundColor: color,
        borderColor: color,
        borderWidth: 1
      }]
    };
  }
}