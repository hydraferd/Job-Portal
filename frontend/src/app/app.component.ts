import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { inject } from '@angular/core';
import { map } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatSidenavModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
  <mat-sidenav-container style="height:100vh">
    <mat-sidenav #nav [mode]="mobile() ? 'over' : 'side'" [opened]="!mobile()">
      <mat-nav-list style="width:220px">
        @for (l of links; track l.path) {
          <a mat-list-item [routerLink]="l.path" routerLinkActive="active" (click)="mobile() && nav.close()">
            <mat-icon matListItemIcon>{{ l.icon }}</mat-icon><span matListItemTitle>{{ l.label }}</span>
          </a>
        }
      </mat-nav-list>
    </mat-sidenav>
    <mat-sidenav-content>
      <mat-toolbar color="primary">
        @if (mobile()) { <button mat-icon-button (click)="nav.toggle()"><mat-icon>menu</mat-icon></button> }
        <span>Job Portal</span>
      </mat-toolbar>
      <router-outlet />
    </mat-sidenav-content>
  </mat-sidenav-container>`,
  styles: ['.active { background: rgba(63,81,181,.12); }']
})
export class AppComponent {
  mobile = toSignal(inject(BreakpointObserver).observe('(max-width: 800px)').pipe(map(r => r.matches)), { initialValue: false });
  links = [
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/jobs', label: 'Jobs', icon: 'work' },
    { path: '/applied', label: 'Applied Jobs', icon: 'task_alt' },
    { path: '/companies', label: 'Companies', icon: 'business' },
    { path: '/profile', label: 'Profile & Resume', icon: 'person' },
    { path: '/credentials', label: 'Credentials', icon: 'key' },
    { path: '/scheduler', label: 'Scheduler & Logs', icon: 'schedule' }
  ];
}
