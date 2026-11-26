import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard.component';
import { JobsComponent } from './pages/jobs.component';
import { CompaniesComponent } from './pages/companies.component';
import { ProfileComponent } from './pages/profile.component';
import { CredentialsComponent } from './pages/credentials.component';
import { SchedulerComponent } from './pages/scheduler.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'jobs', component: JobsComponent, data: { applied: false } },
  { path: 'applied', component: JobsComponent, data: { applied: true } },
  { path: 'companies', component: CompaniesComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'credentials', component: CredentialsComponent },
  { path: 'scheduler', component: SchedulerComponent },
  { path: '**', redirectTo: 'dashboard' }
];
