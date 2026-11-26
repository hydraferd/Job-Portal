# Job Portal — Backend + Angular UI (Phases 1, 2 and 4)

Java 21, Spring Boot 3.3, PostgreSQL, Flyway, JPA, Jsoup, springdoc. **No authentication** (by request):
do not expose it publicly without an IP restriction or gateway; it stores personal data and encrypted credentials.

## Status — read first
- Angular frontend: **built successfully** by the author (`ng build`, Angular 18 + Material).
- Java backend: written but **not compiled or run** by the author (no Maven Central access). Run `mvn verify` first and send back any errors.
- This is NOT the full original spec; see "Not in this release".

## What works
- Angular UI (served by Spring Boot at `/`): Dashboard (totals, company-wise stats), Jobs (filter, pagination, scores), Companies (CRUD),
  Profile & Resume (fetch from URL / upload / download), Credentials (masked), Scheduler & Logs (history, run now, per-run log)
- Scheduler every 2h (`SCHEDULER_CRON`), manual trigger `POST /api/runs/trigger`, history `GET /api/runs`
- Discovery via public read-only APIs: Greenhouse, Lever, Ashby, SmartRecruiters; CUSTOM = static-HTML link scan
- Strategy (`JobConnector`) + Factory (`ConnectorFactory`); add a platform by adding one `@Component`
- Matching (title/skills/location/experience vs `minMatchScore`), dedupe on (company, external id), retries, per-company delay
- Profile, resume fetch (Google Drive "anyone with link" or direct URL) / upload / download
- AES-256-GCM encrypted default portal password and per-platform credentials (never returned; shown as `********`)
- Swagger UI `/swagger-ui.html`, health `/actuator/health`, capability matrix `GET /api/platforms`

## Not in this release
- Registration, login, form filling, submission, manual-action queue, approval modes (Phase 3)
- UI screens for Notifications and Manual approvals (no backend for them yet); dashboard counts for applied/successful/failed/pending/duplicate applications
- Email alerts/reports, AI matching and cover letters (Phase 5)
- Workday, Oracle Recruiting, SuccessFactors: no public job/apply API, so discovery reports "unsupported" in the run log
- SmartRecruiters list API has no description, so matching uses the title only
- JavaScript-rendered custom career pages are not discovered

## Frontend
Prebuilt output is committed in `src/main/resources/static`, so `mvn spring-boot:run` serves the UI. Docker rebuilds it from `frontend/`.
Develop the UI: `cd frontend && npm ci && npm start` (proxies `/api` to localhost:8080); rebuild into the jar with `npx ng build`.

## Run locally
    cp .env.example .env          # set APP_ENCRYPTION_KEY
    docker compose up --build     # http://localhost:8080/swagger-ui.html
Without Docker: start Postgres, then `SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run`.
Tests: `mvn verify`.

## Configure
    PUT  /api/profile      {"fullName":"..","email":"..","experienceYears":5,"skills":"java,spring boot",
                            "locations":"bengaluru,remote","roles":"java developer,backend developer",
                            "expectedCtc":"..","noticePeriodDays":30,"minMatchScore":60,
                            "resumeUrl":"https://drive.google.com/file/d/<id>/view","defaultPortalPassword":".."}
    POST /api/companies    {"name":"Acme","platform":"GREENHOUSE","boardToken":"acme"}
    POST /api/profile/resume/fetch
    POST /api/runs/trigger

## Deploy on Render
1. Push to GitHub. 2. Render: New > Blueprint > pick the repo (reads `render.yaml`).
3. Apply. `APP_ENCRYPTION_KEY` is generated; back it up, losing it makes stored secrets unreadable.
4. Open `https://<service>.onrender.com/swagger-ui.html` and configure as above.
Free web plans sleep and have no disk, which breaks the scheduler and resume storage, so use a paid plan.
