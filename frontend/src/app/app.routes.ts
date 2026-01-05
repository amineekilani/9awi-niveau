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
import { MesRecompensesComponent } from './mes-recompenses/mes-recompenses';
import { MesDefisComponent } from './mes-defis/mes-defis';
import { ClassementComponent } from './classement/classement';
import { MesCoursComponent } from './mes-cours/mes-cours';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { ParcoursDashboardComponent } from './parcours-dashboard/parcours-dashboard.component';
import { ParcoursManagerComponent } from './parcours-manager/parcours-manager.component';
import { ParcoursFormComponent } from './parcours-form/parcours-form.component';
import { ParcoursCatalogueComponent } from './parcours-catalogue/parcours-catalogue.component';
import { MesParcoursComponent } from './mes-parcours/mes-parcours.component';
import { ParcoursDetailComponent } from './parcours-detail/parcours-detail.component';
import { ParcoursEtapesComponent } from './parcours-etapes/parcours-etapes.component';

import { ParcoursProgressionDetailsComponent } from './parcours-progression-details/parcours-progression-details.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'mes-cours', component: MesCoursComponent, canActivate: [authGuard] },
  { path: 'mes-recompenses', component: MesRecompensesComponent, canActivate: [authGuard] },
  { path: 'mes-defis', component: MesDefisComponent, canActivate: [authGuard] },
  { path: 'recommandations', component: RecommendationsComponent, canActivate: [authGuard] },
  { path: 'classement', component: ClassementComponent, canActivate: [authGuard] },
  { path: 'confirm-delete', component: ConfirmDeleteComponent, canActivate: [authGuard] },
  { path: 'formateur-dashboard', component: FormateurDashboardComponent, canActivate: [authGuard] },
  { path: 'cours/nouveau', component: CoursFormComponent, canActivate: [authGuard] },
  { path: 'cours/modifier/:id', component: CoursFormComponent, canActivate: [authGuard] },
  { path: 'cours', redirectTo: '/home', pathMatch: 'full' }, // Rediriger vers home
  { path: 'cours/:id', component: CoursDetailComponent, canActivate: [authGuard] },
  { path: 'module/:id', component: ModuleDetailComponent, canActivate: [authGuard] },
  { path: 'quiz/:quizId/module/:moduleId', component: QuizViewerComponent, canActivate: [authGuard] },

  // Routes pour les parcours d'apprentissage
  { path: 'parcours-dashboard', component: ParcoursDashboardComponent, canActivate: [authGuard] },
  { path: 'parcours', component: ParcoursCatalogueComponent, canActivate: [authGuard] }, // Catalogue pour apprenants
  { path: 'mes-parcours', component: MesParcoursComponent, canActivate: [authGuard] }, // Dashboard personnel apprenants
  { path: 'parcours/nouveau', component: ParcoursFormComponent, canActivate: [authGuard] },
  { path: 'parcours/modifier/:id', component: ParcoursFormComponent, canActivate: [authGuard] },
  { path: 'parcours/gerer/:id', component: ParcoursManagerComponent, canActivate: [authGuard] },
  { path: 'parcours/:id/etapes', component: ParcoursEtapesComponent, canActivate: [authGuard] },
  { path: 'parcours/:id/progression', component: ParcoursProgressionDetailsComponent, canActivate: [authGuard] }, // Nouvelle route
  { path: 'parcours/:id', component: ParcoursDetailComponent, canActivate: [authGuard] }, // Détail pour apprenants

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