import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartComponent } from '../components/chart/chart.component';

@Component({
  selector: 'app-test-charts',
  standalone: true,
  imports: [CommonModule, ChartComponent],
  template: `
    <div class="test-charts-container">
      <h1>🧪 Test des Graphiques</h1>
      
      <div class="charts-grid">
        <div class="chart-card">
          <h3>Graphique en Ligne</h3>
          <app-chart 
            type="line" 
            [data]="lineChartData" 
            height="300px">
          </app-chart>
        </div>
        
        <div class="chart-card">
          <h3>Graphique en Barres</h3>
          <app-chart 
            type="bar" 
            [data]="barChartData" 
            height="300px">
          </app-chart>
        </div>
        
        <div class="chart-card">
          <h3>Graphique en Camembert</h3>
          <app-chart 
            type="pie" 
            [data]="pieChartData" 
            height="300px">
          </app-chart>
        </div>
        
        <div class="chart-card">
          <h3>Graphique en Donut</h3>
          <app-chart 
            type="doughnut" 
            [data]="doughnutChartData" 
            height="300px">
          </app-chart>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .test-charts-container {
      padding: 2rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    
    h1 {
      text-align: center;
      color: #063cdf;
      margin-bottom: 2rem;
    }
    
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 2rem;
    }
    
    .chart-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      border: 1px solid #e2e8f0;
    }
    
    .chart-card h3 {
      margin: 0 0 1rem 0;
      color: #1e293b;
      font-weight: 600;
    }
  `]
})
export class TestChartsComponent implements OnInit {
  lineChartData: any;
  barChartData: any;
  pieChartData: any;
  doughnutChartData: any;

  ngOnInit() {
    this.initializeChartData();
  }

  private initializeChartData() {
    // Données pour le graphique en ligne
    this.lineChartData = {
      labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun'],
      datasets: [{
        label: 'Utilisateurs Actifs',
        data: [65, 59, 80, 81, 56, 55],
        borderColor: '#063cdf',
        backgroundColor: 'rgba(6, 60, 223, 0.1)',
        borderWidth: 2,
        fill: true
      }]
    };

    // Données pour le graphique en barres
    this.barChartData = {
      labels: ['Étudiants', 'Formateurs', 'Admins'],
      datasets: [{
        label: 'Nombre d\'utilisateurs',
        data: [120, 25, 5],
        backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
        borderColor: ['#059669', '#d97706', '#dc2626'],
        borderWidth: 1
      }]
    };

    // Données pour le graphique en camembert
    this.pieChartData = {
      labels: ['Badges Obtenus', 'Badges Restants'],
      datasets: [{
        data: [75, 25],
        backgroundColor: ['#10b981', '#e5e7eb'],
        borderColor: ['#059669', '#d1d5db'],
        borderWidth: 2
      }]
    };

    // Données pour le graphique en donut
    this.doughnutChartData = {
      labels: ['Débutant', 'Intermédiaire', 'Avancé', 'Expert'],
      datasets: [{
        data: [40, 30, 20, 10],
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'],
        borderColor: ['#2563eb', '#059669', '#d97706', '#dc2626'],
        borderWidth: 2
      }]
    };
  }
}