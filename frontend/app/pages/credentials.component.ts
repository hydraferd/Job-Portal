import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Credential } from '../api.service';

@Component({
  standalone: true,
  imports: [FormsModule, MatCardModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  template: `
  <div class="page">
    <mat-card><mat-card-header><mat-card-title>Platform credentials</mat-card-title></mat-card-header><mat-card-content>
      <p class="muted">Stored encrypted (AES-256-GCM) and never shown again. They are used by the application engine, which is not part of this release.</p>
      <div class="row">
        <mat-form-field><mat-label>Platform</mat-label><mat-select [(ngModel)]="platform">
          @for (p of platforms; track p) { <mat-option [value]="p">{{ p }}</mat-option> }</mat-select></mat-form-field>
        <mat-form-field><mat-label>Username / email</mat-label><input matInput [(ngModel)]="username"></mat-form-field>
        <mat-form-field><mat-label>Password</mat-label><input matInput type="password" autocomplete="new-password" [(ngModel)]="password"></mat-form-field>
        <button mat-flat-button (click)="save()">Save</button>
      </div>
    </mat-card-content></mat-card>
    <mat-card><mat-card-content class="scroll"><table mat-table [dataSource]="rows()" class="full">
      <ng-container matColumnDef="platform"><th mat-header-cell *matHeaderCellDef>Platform</th><td mat-cell *matCellDef="let c">{{ c.platform }}</td></ng-container>
      <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>Username</th><td mat-cell *matCellDef="let c">{{ c.username }}</td></ng-container>
      <ng-container matColumnDef="password"><th mat-header-cell *matHeaderCellDef>Password</th><td mat-cell *matCellDef="let c">{{ c.password }}</td></ng-container>
      <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let c">
        <button mat-icon-button (click)="remove(c)"><mat-icon>delete</mat-icon></button></td></ng-container>
      <tr mat-header-row *matHeaderRowDef="cols"></tr><tr mat-row *matRowDef="let r; columns: cols"></tr>
    </table></mat-card-content></mat-card>
  </div>`
})
export class CredentialsComponent implements OnInit {
  private api = inject(ApiService); private bar = inject(MatSnackBar);
  rows = signal<Credential[]>([]);
  cols = ['platform', 'username', 'password', 'actions'];
  platforms = ['GREENHOUSE', 'LEVER', 'ASHBY', 'SMARTRECRUITERS', 'WORKDAY', 'ORACLE_RECRUITING', 'SUCCESSFACTORS', 'CUSTOM'];
  platform = 'GREENHOUSE'; username = ''; password = '';
  ngOnInit() { this.load(); }
  load() { this.api.credentials().subscribe(c => this.rows.set(c)); }
  save() {
    this.api.saveCredential(this.platform, this.username, this.password).subscribe(() => {
      this.password = ''; this.bar.open('Saved', undefined, { duration: 2000 }); this.load();
    });
  }
  remove(c: Credential) { this.api.deleteCredential(c.platform).subscribe(() => this.load()); }
}
