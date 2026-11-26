import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ApiService, Stats } from '../api.service';

@Component({
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatProgressBarModule, DatePipe],
  template: `
  <div class="page">
    @if (s(); as s) {
      <div class="grid">
        <mat-card><mat-card-content><div class="muted">Companies tracked</div><p class="big">{{ s.companies }}</p></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="muted">Relevant jobs discovered</div><p class="big">{{ s.discovered }}</p></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="muted">Matched to profile</div><p class="big">{{ s.matched }}</p></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="muted">Last run</div>
          <p class="big" style="font-size:22px">{{ s.lastRun?.status ?? 'Never' }}</p>
          <div class="muted">{{ s.lastRun?.startedAt | date:'medium' }}</div></mat-card-content></mat-card>
      </div>
      <mat-card>
        <mat-card-header><mat-card-title>Company-wise statistics</mat-card-title></mat-card-header>
        <mat-card-content class="scroll">
          <table mat-table [dataSource]="s.byCompany" class="full">
            <ng-container matColumnDef="company"><th mat-header-cell *matHeaderCellDef>Company</th><td mat-cell *matCellDef="let r">{{ r.company }}</td></ng-container>
            <ng-container matColumnDef="discovered"><th mat-header-cell *matHeaderCellDef>Discovered</th><td mat-cell *matCellDef="let r">{{ r.discovered }}</td></ng-container>
            <ng-container matColumnDef="matched"><th mat-header-cell *matHeaderCellDef>Matched</th><td mat-cell *matCellDef="let r">{{ r.matched }}</td></ng-container>
            <ng-container matColumnDef="ratio"><th mat-header-cell *matHeaderCellDef style="width:30%">Match ratio</th>
              <td mat-cell *matCellDef="let r"><mat-progress-bar mode="determinate" [value]="r.discovered ? 100 * r.matched / r.discovered : 0" /></td></ng-container>
            <tr mat-header-row *matHeaderRowDef="cols"></tr><tr mat-row *matRowDef="let r; columns: cols"></tr>
          </table>
          @if (!s.byCompany.length) { <p class="muted">No jobs yet. Add companies, then run discovery from Scheduler.</p> }
        </mat-card-content>
      </mat-card>
      <p class="muted">Applied / successful / failed / pending / duplicate application counts arrive with the application engine (not in this release).</p>
    }
  </div>`
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  s = signal<Stats | null>(null);
  cols = ['company', 'discovered', 'matched', 'ratio'];
  ngOnInit() { this.api.stats().subscribe(x => this.s.set(x)); }
}
