import { Component, Input, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-container" [style.height]="height">
      <canvas #chartCanvas></canvas>
    </div>
  `,
  styles: [`
    .chart-container {
      position: relative;
      width: 100%;
    }
    
    canvas {
      max-width: 100%;
      height: auto !important;
    }
  `]
})
export class ChartComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas', { static: true }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  @Input() type: ChartType = 'line';
  @Input() data: any;
  @Input() options: any = {};
  @Input() height: string = '300px';
  @Input() responsive: boolean = true;
  
  private chart: Chart | null = null;

  ngOnInit() {
    // Configuration par défaut
    this.setDefaultOptions();
  }

  ngAfterViewInit() {
    this.createChart();
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  private setDefaultOptions() {
    const defaultOptions = {
      responsive: this.responsive,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top' as const,
          labels: {
            usePointStyle: true,
            padding: 20,
            font: {
              size: 12,
              family: 'Inter, sans-serif'
            }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#fff',
          bodyColor: '#fff',
          borderColor: '#063cdf',
          borderWidth: 1,
          cornerRadius: 8,
          displayColors: true,
          padding: 12
        }
      },
      scales: this.getScalesConfig()
    };

    this.options = { ...defaultOptions, ...this.options };
  }

  private getScalesConfig() {
    if (this.type === 'pie' || this.type === 'doughnut') {
      return {};
    }

    return {
      x: {
        grid: {
          display: true,
          color: 'rgba(0, 0, 0, 0.1)'
        },
        ticks: {
          font: {
            size: 11,
            family: 'Inter, sans-serif'
          },
          color: '#6b7280'
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          display: true,
          color: 'rgba(0, 0, 0, 0.1)'
        },
        ticks: {
          font: {
            size: 11,
            family: 'Inter, sans-serif'
          },
          color: '#6b7280'
        }
      }
    };
  }

  private createChart() {
    if (!this.chartCanvas || !this.data) {
      return;
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const config: ChartConfiguration = {
      type: this.type,
      data: this.data,
      options: this.options
    };

    this.chart = new Chart(ctx, config);
  }

  updateChart(newData: any) {
    if (this.chart) {
      this.chart.data = newData;
      this.chart.update();
    }
  }

  updateOptions(newOptions: any) {
    if (this.chart) {
      this.chart.options = { ...this.chart.options, ...newOptions };
      this.chart.update();
    }
  }
}