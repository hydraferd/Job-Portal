import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Company { id?: number; name: string; platform: string; boardToken?: string; careersUrl?: string; enabled: boolean; lastSyncAt?: string; }
export interface Job { id: number; company: Company; title: string; location: string; url: string; postedAt?: string; discoveredAt: string; matchScore: number; matched: boolean; applied: boolean; appliedAt?: string; }
export interface Page<T> { content: T[]; totalElements: number; }
export interface Run { id: number; triggerType: string; startedAt: string; finishedAt?: string; status: string; companiesScanned: number; jobsFound: number; newJobs: number; matchedJobs: number; duplicates: number; failures: number; log?: string; }
export interface Stats { companies: number; discovered: number; matched: number; lastRun: Run | null; byCompany: { company: string; discovered: number; matched: number }[]; }
export interface PlatformInfo { platform: string; discovery: boolean; autoApply: boolean; note: string; }
export interface Profile { fullName: string; email: string; phone: string; experienceYears: number; skills: string; locations: string; roles: string; expectedCtc: string; noticePeriodDays: number; minMatchScore: number; resumeUrl: string; resumeFileName: string; defaultPortalUsername: string; defaultPortalEmail: string; defaultPortalPassword: string | null; defaultPortalPasswordSet: boolean; }
export interface Credential { platform: string; username: string; password: string; updatedAt: string; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  stats = () => this.http.get<Stats>('/api/stats');
  platforms = () => this.http.get<PlatformInfo[]>('/api/platforms');
  jobs = (matched: boolean | null, applied: boolean, page: number, size: number) =>
    this.http.get<Page<Job>>('/api/jobs', { params: { page, size, applied, ...(matched === null ? {} : { matched }) } });
  setApplied = (id: number, applied: boolean) => this.http.patch<Job>(`/api/jobs/${id}/applied`, { applied });
  companies = () => this.http.get<Company[]>('/api/companies');
  saveCompany = (c: Company) => c.id ? this.http.put<Company>(`/api/companies/${c.id}`, c) : this.http.post<Company>('/api/companies', c);
  testCompany = (id: number) => this.http.post<{ found: number; sample: string[] }>(`/api/companies/${id}/test`, {});
  deleteCompany = (id: number) => this.http.delete(`/api/companies/${id}`);
  profile = () => this.http.get<Profile>('/api/profile');
  saveProfile = (p: Profile) => this.http.put<Profile>('/api/profile', p);
  fetchResume = (url: string) => this.http.post<Profile>('/api/profile/resume/fetch', { url });
  uploadResume = (f: File) => { const d = new FormData(); d.append('file', f); return this.http.post<Profile>('/api/profile/resume/upload', d); };
  credentials = () => this.http.get<Credential[]>('/api/credentials');
  saveCredential = (platform: string, username: string, password: string) => this.http.put<Credential>(`/api/credentials/${platform}`, { username, password });
  deleteCredential = (platform: string) => this.http.delete(`/api/credentials/${platform}`);
  runs = () => this.http.get<Run[]>('/api/runs');
  trigger = () => this.http.post('/api/runs/trigger', {});
}
