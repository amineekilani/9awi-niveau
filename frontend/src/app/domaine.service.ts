import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DomaineService {
  private apiUrl = `${environment.apiUrl}/domaines`;

  constructor(private http: HttpClient) { }

  getAllDomaines(): Observable<string[]> {
    return this.http.get<string[]>(this.apiUrl);
  }

  getDomainIcon(domaine: string): string {
    const iconMap: { [key: string]: string } = {
      'Développement Web': 'code',
      'Développement Mobile': 'smartphone',
      'Data Science': 'trending-up',
      'Intelligence Artificielle': 'cpu',
      'Design UI/UX': 'palette',
      'Cybersécurité': 'shield',
      'DevOps': 'server',
      'Marketing Digital': 'megaphone',
      'Gestion de Projet': 'clipboard',
      'Business Intelligence': 'bar-chart',
      'Réseaux et Systèmes': 'wifi',
      'Base de Données': 'database'
    };
    return iconMap[domaine] || 'book';
  }

  getDomainColor(domaine: string): string {
    const colorMap: { [key: string]: string } = {
      'Développement Web': '#3B82F6',
      'Développement Mobile': '#8B5CF6',
      'Data Science': '#10B981',
      'Intelligence Artificielle': '#F59E0B',
      'Design UI/UX': '#EF4444',
      'Cybersécurité': '#6B7280',
      'DevOps': '#059669',
      'Marketing Digital': '#DC2626',
      'Gestion de Projet': '#7C3AED',
      'Business Intelligence': '#0891B2',
      'Réseaux et Systèmes': '#EA580C',
      'Base de Données': '#065F46'
    };
    return colorMap[domaine] || '#6B7280';
  }
}