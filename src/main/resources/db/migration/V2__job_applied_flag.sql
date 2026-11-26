alter table job add column applied boolean not null default false;
alter table job add column applied_at timestamp with time zone;
create index idx_job_applied on job(applied);
