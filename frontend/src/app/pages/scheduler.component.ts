import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Run } from '../api.service';

@Component({
  standalone: true,
  imports: [DatePipe, MatCardModule, MatTableModule, MatButtonModule, MatExpansionModule],
  template: `
  <div class="page">
    <mat-card><mat-card-content class="row">
      <button mat-flat-button [disabled]="running()" (click)="trigger()">{{ running() ? 'Running…' : 'Run discovery now' }}</button>
      <span class="muted">Automatic runs follow SCHEDULER_CRON (default every 2 hours, UTC). Change it via environment variables and restart.</span>
    </mat-card-content></mat-card>
    <mat-card><mat-card-header><mat-card-title>Execution history</mat-card-title></mat-card-header><mat-card-content class="scroll">
      <table mat-table [dataSource]="rows()" class="full">
        <ng-container matColumnDef="started"><th mat-header-cell *matHeaderCellDef>Started</th><td mat-cell *matCellDef="let r">{{ r.startedAt | date:'short' }}</td></ng-container>
        <ng-container matColumnDef="trigger"><th mat-header-cell *matHeaderCellDef>Trigger</th><td mat-cell *matCellDef="let r">{{ r.triggerType }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let r">{{ r.status }}</td></ng-container>
        <ng-container matColumnDef="companies"><th mat-header-cell *matHeaderCellDef>Companies</th><td mat-cell *matCellDef="let r">{{ r.companiesScanned }}</td></ng-container>
        <ng-container matColumnDef="found"><th mat-header-cell *matHeaderCellDef>Found</th><td mat-cell *matCellDef="let r">{{ r.jobsFound }}</td></ng-container>
        <ng-container matColumnDef="new"><th mat-header-cell *matHeaderCellDef>New</th><td mat-cell *matCellDef="let r">{{ r.newJobs }}</td></ng-container>
        <ng-container matColumnDef="matched"><th mat-header-cell *matHeaderCellDef>Matched</th><td mat-cell *matCellDef="let r">{{ r.matchedJobs }}</td></ng-container>
        <ng-container matColumnDef="dups"><th mat-header-cell *matHeaderCellDef>Duplicates</th><td mat-cell *matCellDef="let r">{{ r.duplicates }}</td></ng-container>
        <ng-container matColumnDef="fail"><th mat-header-cell *matHeaderCellDef>Failures</th><td mat-cell *matCellDef="let r">{{ r.failures }}</td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr><tr mat-row *matRowDef="let r; columns: cols"></tr>
      </table>
    </mat-card-content></mat-card>
    <mat-card><mat-card-header><mat-card-title>Execution logs</mat-card-title></mat-card-header><mat-card-content>
      <mat-accordion>
        @for (r of withLogs(); track r.id) {
          <mat-expansion-panel><mat-expansion-panel-header>{{ r.startedAt | date:'short' }} · {{ r.status }}</mat-expansion-panel-header>
            <pre class="log">{{ r.log }}</pre></mat-expansion-panel>
        }
      </mat-accordion>
      @if (!withLogs().length) { <p class="muted">No errors or warnings logged.</p> }
    </mat-card-content></mat-card>
  </div>`
})
export class SchedulerComponent implements OnInit, OnDestroy {
  private api = inject(ApiService); private bar = inject(MatSnackBar);
  rows = signal<Run[]>([]);
  cols = ['started', 'trigger', 'status', 'companies', 'found', 'new', 'matched', 'dups', 'fail'];
  private timer?: ReturnType<typeof setInterval>;
  running = () => this.rows().some(r => r.status === 'RUNNING');
  withLogs = () => this.rows().filter(r => r.log);
  ngOnInit() { this.load(); this.timer = setInterval(() => this.load(), 5000); }
  ngOnDestroy() { clearInterval(this.timer); }
  load() { this.api.runs().subscribe(r => this.rows.set(r)); }
  trigger() {
    this.api.trigger().subscribe({
      next: () => { this.bar.open('Discovery started', undefined, { duration: 2000 }); setTimeout(() => this.load(), 800); },
      error: (err) => this.bar.open(err?.error?.error || 'Failed to start discovery', 'OK', { duration: 4000 })
    });
  }
}
