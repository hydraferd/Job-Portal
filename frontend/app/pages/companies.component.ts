import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Company, PlatformInfo } from '../api.service';

@Component({
  standalone: true,
  imports: [FormsModule, DatePipe, MatCardModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSlideToggleModule],
  template: `
  <div class="page">
    <mat-card><mat-card-header><mat-card-title>{{ form.id ? 'Edit' : 'Add' }} company</mat-card-title></mat-card-header>
    <mat-card-content>
      <div class="row">
        <mat-form-field><mat-label>Name</mat-label><input matInput [(ngModel)]="form.name"></mat-form-field>
        <mat-form-field><mat-label>Platform</mat-label>
          <mat-select [(ngModel)]="form.platform">
            @for (p of platforms(); track p.platform) { <mat-option [value]="p.platform">{{ p.platform }}{{ p.discovery ? '' : ' (unsupported)' }}</mat-option> }
          </mat-select></mat-form-field>
        <mat-form-field><mat-label>Board token / company id</mat-label><input matInput [(ngModel)]="form.boardToken"></mat-form-field>
        <mat-form-field><mat-label>Careers URL (WORKDAY / CUSTOM)</mat-label><input matInput [(ngModel)]="form.careersUrl"></mat-form-field>
        <mat-slide-toggle [(ngModel)]="form.enabled">Enabled</mat-slide-toggle>
        <button mat-flat-button (click)="save()">Save</button>
        @if (form.id) { <button mat-button (click)="reset()">Cancel</button> }
      </div>
      <p class="muted">Board token is the slug in the public job-board URL (e.g. boards.greenhouse.io/<b>acme</b>, jobs.lever.co/<b>acme</b>). For WORKDAY paste the careers site URL, e.g. https://<b>acme</b>.wd3.myworkdayjobs.com/en-US/<b>Acme_Careers</b>.</p>
    </mat-card-content></mat-card>
    <mat-card><mat-card-content class="scroll">
      <table mat-table [dataSource]="rows()" class="full">
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let c">{{ c.name }}</td></ng-container>
        <ng-container matColumnDef="platform"><th mat-header-cell *matHeaderCellDef>Platform</th><td mat-cell *matCellDef="let c">{{ c.platform }}</td></ng-container>
        <ng-container matColumnDef="enabled"><th mat-header-cell *matHeaderCellDef>Enabled</th><td mat-cell *matCellDef="let c">{{ c.enabled ? 'Yes' : 'No' }}</td></ng-container>
        <ng-container matColumnDef="sync"><th mat-header-cell *matHeaderCellDef>Last sync</th><td mat-cell *matCellDef="let c">{{ c.lastSyncAt ? (c.lastSyncAt | date:'short') : '-' }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let c">
          <button mat-icon-button title="Test connection" (click)="test(c)"><mat-icon>bolt</mat-icon></button>
          <button mat-icon-button (click)="edit(c)"><mat-icon>edit</mat-icon></button>
          <button mat-icon-button (click)="remove(c)"><mat-icon>delete</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr><tr mat-row *matRowDef="let r; columns: cols"></tr>
      </table>
    </mat-card-content></mat-card>
  </div>`
})
export class CompaniesComponent implements OnInit {
  private api = inject(ApiService); private bar = inject(MatSnackBar);
  rows = signal<Company[]>([]); platforms = signal<PlatformInfo[]>([]);
  cols = ['name', 'platform', 'enabled', 'sync', 'actions'];
  form: Company = this.blank();
  blank(): Company { return { name: '', platform: 'GREENHOUSE', boardToken: '', careersUrl: '', enabled: true }; }
  ngOnInit() { this.api.platforms().subscribe(p => this.platforms.set(p)); this.load(); }
  load() { this.api.companies().subscribe(c => this.rows.set(c)); }
  save() { this.api.saveCompany(this.form).subscribe(() => { this.bar.open('Saved', undefined, { duration: 2000 }); this.reset(); this.load(); }); }
  test(c: Company) {
    this.bar.open(`Testing ${c.name}...`, undefined, { duration: 2000 });
    this.api.testCompany(c.id!).subscribe(r => this.bar.open(
      `OK: ${r.found} job(s) returned` + (r.sample.length ? ` e.g. ${r.sample[0]}` : ''), 'Close', { duration: 10000 }));
  }
  edit(c: Company) { this.form = { ...c }; }
  reset() { this.form = this.blank(); }
  remove(c: Company) { if (confirm(`Delete ${c.name} and its stored jobs?`)) this.api.deleteCompany(c.id!).subscribe(() => this.load()); }
}
