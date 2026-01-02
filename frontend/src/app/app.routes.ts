import { Routes } from '@angular/router';
import { LoginComponent } from './login/login';
import { HomeComponent } from './home/home';
import { authGuard } from './auth-guard';
import { adminGuard } from './admin-guard';
import { RegisterComponent } from './register/register';
import { VerifyEmailComponent } from './verify-email/verify-email';
import { ForgotPasswordComponent } from './forgot-password/forgot-password';
import { ResetPasswordComponent } from './reset-password/reset-password';
import { ProfileComponent } from './profile/profile';
import { ConfirmDeleteComponent } from './confirm-delete/confirm-delete';
import { FormateurDashboardComponent } from './formateur-dashboard/formateur-dashboard';
import { CoursFormComponent } from './cours-form/cours-form';
import { CoursListComponent } from './cours-list/cours-list';
import { CoursDetailComponent } from './cours-detail/cours-detail';
import { ModuleDetailComponent } from './module-detail/module-detail';
import { QuizViewerComponent } from './quiz-viewer/quiz-viewer';
import { AdminMainComponent } from './admin-main/admin-main';
import { AdminDashboardMainComponent } from './admin-dashboard-main/admin-dashboard-main';
import { AdminUsersComponent } from './admin-users/admin-users';
import { AdminGamificationComponent } from './admin-gamification/admin-gamification';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'confirm-delete', component: ConfirmDeleteComponent, canActivate: [authGuard] },
  { path: 'formateur-dashboard', component: FormateurDashboardComponent, canActivate: [authGuard] },
  { path: 'cours/nouveau', component: CoursFormComponent, canActivate: [authGuard] },
  { path: 'cours/modifier/:id', component: CoursFormComponent, canActivate: [authGuard] },
  { path: 'cours/:id', component: CoursDetailComponent, canActivate: [authGuard] },
  { path: 'cours', component: CoursListComponent, canActivate: [authGuard] },
  { path: 'module/:id', component: ModuleDetailComponent, canActivate: [authGuard] },
  { path: 'quiz/:quizId/module/:moduleId', component: QuizViewerComponent, canActivate: [authGuard] },
  
  // Routes Admin avec layout unifié
  { 
    path: 'admin', 
    component: AdminMainComponent, 
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardMainComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'gamification', component: AdminGamificationComponent }
    ]
  }
];