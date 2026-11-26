import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Profile } from '../api.service';

@Component({
  standalone: true,
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
  <div class="page">
  @if (p(); as p) {
    <mat-card><mat-card-header><mat-card-title>Candidate profile</mat-card-title></mat-card-header><mat-card-content>
      <div class="row">
        <mat-form-field><mat-label>Full name</mat-label><input matInput [(ngModel)]="p.fullName"></mat-form-field>
        <mat-form-field><mat-label>Email</mat-label><input matInput [(ngModel)]="p.email"></mat-form-field>
        <mat-form-field><mat-label>Phone</mat-label><input matInput [(ngModel)]="p.phone"></mat-form-field>
      </div>
      <div class="row">
        <mat-form-field><mat-label>Experience (years)</mat-label><input matInput type="number" [(ngModel)]="p.experienceYears"></mat-form-field>
        <mat-form-field><mat-label>Expected CTC</mat-label><input matInput [(ngModel)]="p.expectedCtc"></mat-form-field>
        <mat-form-field><mat-label>Notice period (days)</mat-label><input matInput type="number" [(ngModel)]="p.noticePeriodDays"></mat-form-field>
        <mat-form-field><mat-label>Min match score (0-100)</mat-label><input matInput type="number" [(ngModel)]="p.minMatchScore"></mat-form-field>
      </div>
      <mat-form-field class="full"><mat-label>Target roles (comma separated, matched against job titles)</mat-label><textarea matInput rows="2" [(ngModel)]="p.roles"></textarea></mat-form-field>
      <mat-form-field class="full"><mat-label>Skills (comma separated)</mat-label><textarea matInput rows="2" [(ngModel)]="p.skills"></textarea></mat-form-field>
      <mat-form-field class="full"><mat-label>Preferred locations (comma separated)</mat-label><input matInput [(ngModel)]="p.locations"></mat-form-field>
      <h3>Default portal account</h3>
      <div class="row">
        <mat-form-field><mat-label>Username</mat-label><input matInput [(ngModel)]="p.defaultPortalUsername"></mat-form-field>
        <mat-form-field><mat-label>Email</mat-label><input matInput [(ngModel)]="p.defaultPortalEmail"></mat-form-field>
        <mat-form-field><mat-label>Password</mat-label><input matInput type="password" autocomplete="new-password" [(ngModel)]="p.defaultPortalPassword"
          [placeholder]="p.defaultPortalPasswordSet ? '******** (saved, leave blank to keep)' : ''"></mat-form-field>
      </div>
      <button mat-flat-button (click)="save()">Save profile</button>
    </mat-card-content></mat-card>
    <mat-card><mat-card-header><mat-card-title>Resume</mat-card-title></mat-card-header><mat-card-content>
      <p>Current file: <b>{{ p.resumeFileName || 'none' }}</b>
        @if (p.resumeFileName) { · <a href="/api/profile/resume/download">download</a> }</p>
      <div class="row">
        <mat-form-field><mat-label>Google Drive (anyone with link) or direct URL</mat-label><input matInput [(ngModel)]="p.resumeUrl"></mat-form-field>
        <button mat-stroked-button (click)="fetch()">Fetch from URL</button>
      </div>
      <div class="row"><input type="file" accept=".pdf,.doc,.docx" (change)="upload($event)"></div>
    </mat-card-content></mat-card>
  }
  </div>`
})
export class ProfileComponent implements OnInit {
  private api = inject(ApiService); private bar = inject(MatSnackBar);
  p = signal<Profile | null>(null);
  ngOnInit() { this.api.profile().subscribe(x => this.p.set(x)); }
  private done(x: Profile, msg: string) { this.p.set(x); this.bar.open(msg, undefined, { duration: 2500 }); }
  save() { this.api.saveProfile(this.p()!).subscribe(x => this.done(x, 'Profile saved')); }
  fetch() {
    const cur = this.p()!;
    this.api.saveProfile(cur).subscribe(() => this.api.fetchResume(cur.resumeUrl).subscribe(x => this.done(x, 'Resume downloaded')));
  }
  upload(e: Event) {
    const f = (e.target as HTMLInputElement).files?.[0];
    if (f) this.api.uploadResume(f).subscribe(x => this.done(x, 'Resume uploaded'));
  }
}
