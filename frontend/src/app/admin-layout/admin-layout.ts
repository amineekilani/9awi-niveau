import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.html',
  styleUrls: ['./admin-layout.css']
})
export class AdminLayoutComponent implements OnInit {
  currentUser: any = {};
  sidebarCollapsed = false;
  
  menuItems = [
    {
      title: 'Dashboard',
      icon: 'dashboard',
      route: '/admin/dashboard',
      active: true
    },
    {
      title: 'Utilisateurs',
      icon: 'users',
      route: '/admin/users',
      active: false
    },
    {
      title: 'Gamification',
      icon: 'trophy',
      route: '/admin/gamification',
      active: false
    },
    {
      title: 'Cours',
      icon: 'book',
      route: '/admin/courses',
      active: false
    },
    {
      title: 'Rapports',
      icon: 'chart',
      route: '/admin/reports',
      active: false
    },
    {
      title: 'Paramètres',
      icon: 'settings',
      route: '/admin/settings',
      active: false
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.currentUser = {
      email: this.authService.getEmail(),
      role: this.authService.getRole()
    };
    
    // Set active menu item based on current route
    this.updateActiveMenuItem();
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveMenuItem(item: any) {
    this.menuItems.forEach(menuItem => menuItem.active = false);
    item.active = true;
    this.router.navigate([item.route]);
  }

  updateActiveMenuItem() {
    const currentRoute = this.router.url;
    this.menuItems.forEach(item => {
      item.active = currentRoute.startsWith(item.route);
    });
  }

  logout() {
    this.authService.logout();
  }

  getIconClass(icon: string): string {
    const iconMap: { [key: string]: string } = {
      'dashboard': '📊',
      'users': '👥',
      'trophy': '🏆',
      'book': '📚',
      'chart': '📈',
      'settings': '⚙️'
    };
    return iconMap[icon] || '📄';
  }
}