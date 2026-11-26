import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ActivatedRoute } from '@angular/router';
import { ApiService, Job } from '../api.service';

@Component({
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatPaginatorModule, MatButtonToggleModule, MatChipsModule, MatIconModule, MatCheckboxModule, MatButtonModule, DatePipe],
  template: `
  <div class="page"><mat-card><mat-card-content>
    <div class="row">
      <mat-button-toggle-group [value]="filter" (change)="setFilter($event.value)">
        <mat-button-toggle value="all">All</mat-button-toggle>
        <mat-button-toggle value="matched">Matched</mat-button-toggle>
        <mat-button-toggle value="unmatched">Below threshold</mat-button-toggle>
      </mat-button-toggle-group>
    </div>
    <div class="scroll"><table mat-table [dataSource]="rows()" class="full">
      <ng-container matColumnDef="applied"><th mat-header-cell *matHeaderCellDef>Applied</th>
        <td mat-cell *matCellDef="let j"><mat-checkbox [checked]="j.applied" (change)="toggle(j, $event.checked)" /></td></ng-container>
      <ng-container matColumnDef="company"><th mat-header-cell *matHeaderCellDef>Company</th><td mat-cell *matCellDef="let j">{{ j.company.name }}</td></ng-container>
      <ng-container matColumnDef="title"><th mat-header-cell *matHeaderCellDef>Title</th><td mat-cell *matCellDef="let j"><a [href]="j.url" target="_blank" rel="noopener">{{ j.title }}</a></td></ng-container>
      <ng-container matColumnDef="location"><th mat-header-cell *matHeaderCellDef>Location</th><td mat-cell *matCellDef="let j">{{ j.location }}</td></ng-container>
      <ng-container matColumnDef="score"><th mat-header-cell *matHeaderCellDef>Score</th>
        <td mat-cell *matCellDef="let j"><mat-chip [highlighted]="j.matched">{{ j.matchScore }}</mat-chip></td></ng-container>
      <ng-container matColumnDef="apply"><th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let j"><a mat-stroked-button [href]="j.url" target="_blank" rel="noopener">Apply</a></td></ng-container>
      <ng-container matColumnDef="found"><th mat-header-cell *matHeaderCellDef>Age</th><td mat-cell *matCellDef="let j" [title]="(j.postedAt || j.discoveredAt) | date:'medium'">{{ age(j) }}</td></ng-container>
      <tr mat-header-row *matHeaderRowDef="cols"></tr><tr mat-row *matRowDef="let r; columns: cols"></tr>
    </table></div>
    <mat-paginator [length]="total()" [pageSize]="size" [pageSizeOptions]="[10,25,50,100]" (page)="onPage($event)" />
  </mat-card-content></mat-card></div>`
})
export class JobsComponent implements OnInit {
  private api = inject(ApiService);
  rows = signal<Job[]>([]); total = signal(0);
  cols = ['applied', 'company', 'title', 'location', 'score', 'found', 'apply'];
  private appliedTab = inject(ActivatedRoute).snapshot.data['applied'] === true;
  filter = 'all'; page = 0; size = 25;
  ngOnInit() { this.load(); }
  setFilter(f: string) { this.filter = f; this.page = 0; this.load(); }
  onPage(e: PageEvent) { this.page = e.pageIndex; this.size = e.pageSize; this.load(); }
  toggle(j: Job, checked: boolean) { this.api.setApplied(j.id, checked).subscribe(() => this.load()); }
  age(j: Job): string {
    const d = Math.floor((Date.now() - new Date(j.postedAt ?? j.discoveredAt).getTime()) / 86400000);
    const when = d <= 0 ? 'today' : d === 1 ? '1 day ago' : `${d} days ago`;
    return (j.postedAt ? 'Posted ' : 'Found ') + when;
  }
  load() {
    const m = this.filter === 'all' ? null : this.filter === 'matched';
    this.api.jobs(m, this.appliedTab, this.page, this.size).subscribe(p => { this.rows.set(p.content); this.total.set(p.totalElements); });
  }
}
