# PRMS — Placement Records Management System

A full-stack placement management system for IIIT Guwahati: tracks students, companies, placement drives, multi-round applications, HR contacts, eligibility criteria, and tier-based placement policy, with role-based dashboards for Admins, Placement Officers, department Staff, and Students.

This is the current, actively-developed application. (A separate, earlier Java Swing + MySQL desktop prototype for the same coursework project also lives in this repository — see [Legacy prototype](#legacy-prototype) below for where to find it and why it's kept.)

---

## Tech stack

| Layer     | Stack |
|-----------|-------|
| Backend   | Java 21, Spring Boot 3.5.14 (Web, Data JPA, Security, Validation, Mail), Maven |
| Auth      | JWT (`io.jsonwebtoken`), Spring Security, role-based access control |
| Database  | MySQL (`placement_records_db`), Hibernate (`ddl-auto=update`, no separate migration tool) |
| Frontend  | React 19 + Vite, React Router, Bootstrap 5, Recharts (dashboard charts), lucide-react (icons) |
| Integrations | Google Sheets API (drive-submission and student-registration Google Forms), SMTP mail (welcome emails, reminders, status notifications) |
| API docs  | springdoc-openapi — Swagger UI at `http://localhost:8080/swagger-ui/index.html` once the backend is running |

---

## Features by role

- **Admin** — full CRUD over students, companies, drives, applications, departments, penalties, HR contacts, eligibility criteria; manages Placement Officer and department Staff portal accounts (create, enable/disable, reset password, welcome email); views the full analytics dashboard; configures automatic drive-reminder emails.
- **Placement Officer** — same operational access as Admin over students/companies/drives/applications/dashboard, minus account-provisioning screens.
- **Staff** *(shared, single read-only account)* — dashboard-only visibility into placement statistics; no edit access anywhere.
- **Student** — self-service portal: view/update profile, browse eligible drives, track their own applications, see placement status and companies they've applied to.

**Core functionality:**
- Student and drive data can be imported automatically from linked Google Forms (via Google Sheets API polling), in addition to manual admin entry.
- Multi-round application status tracking (Applied → First Round → Interview Scheduled → Selected/Rejected), with automatic email notifications on status changes and upcoming-drive reminders.
- Placement Dashboard: KPIs, tier distribution, placement trends, branch-wise and program×branch (B.Tech/M.Tech × CSE/ECE) placement statistics, real salary analytics from actual placements, top recruiters, and full companies-placed list. See [`docs/flow.md`](docs/flow.md) for exactly how a dashboard request is computed end-to-end, and [`docs/decisions.md`](docs/decisions.md) for the reasoning behind its non-obvious modeling choices.
- Tier-based placement policy (Normal / Dream / Super Dream) with eligibility snapshots (CGPA + backlog based).
- Penalty tracking, external HR contact directory, and configurable automatic drive-event reminder emails.

---

## Project structure

```
prms2/
├── backend/            Spring Boot REST API (Java 21, Maven)
│   └── src/main/java/com/pmrs/backend/
│       ├── controller/  REST endpoints
│       ├── service/     business logic
│       ├── entity/      JPA entities
│       ├── repository/  Spring Data JPA repositories
│       ├── dto/         request/response DTOs
│       ├── security/    JWT filter + Spring Security config
│       └── config/      startup config (demo data seeding, one-time migrations)
├── frontend/           React + Vite SPA
│   └── src/
│       ├── pages/        route-level views (per role)
│       ├── components/   shared UI (Navbar, Sidebar, Layout, ...)
│       ├── routes/       role-based route guards
│       └── services/     axios API clients, one per backend resource
├── docs/               decision log, execution-flow docs, and coursework deliverable documents
└── (legacy prototype files — see below)
```

---

## Getting started

### Prerequisites
- Java 21 (JDK)
- Node.js (18+) and npm
- MySQL Server, running locally with a `placement_records_db` schema created (Hibernate creates/updates tables automatically on startup — no manual DDL needed)

### Backend
1. Create `backend/.env` (gitignored, never commit) with:
   ```
   DB_USERNAME=<your-mysql-user>
   DB_PASSWORD=<your-mysql-password>
   MAIL_USERNAME=<gmail-address-for-outgoing-mail>
   MAIL_PASSWORD=<gmail-app-password>
   ```
   Set `prms.mail.enabled=false` in `application.properties` if you don't want to configure mail for a local run — all email sends become no-ops.
2. *(Optional, only needed for the Google Forms import features)* place a Google service-account key at `backend/src/main/resources/google-service-account.json` (also gitignored).
3. From `backend/`, run:
   ```
   ./mvnw spring-boot:run
   ```
   Starts on `http://localhost:8080`. On first run (outside a `prod` profile), demo accounts are seeded automatically — see below.

### Frontend
From `frontend/`, run:
```
npm install
npm run dev
```
Starts on `http://localhost:5173` and talks to the backend at `http://localhost:8080`.

### Demo accounts (seeded automatically in non-prod)
| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Placement Officer | `officer` | `officer123` |
| Staff | `staff` | `staff123` |
| Student | `student` | `student123` |

---

## Legacy prototype

The repository root also contains an earlier, standalone Java Swing + MySQL desktop application (`Main.java`, `db/`, `ui/`, `sql/`, `lib/`, `out/`, `compile.bat`/`run.bat`/`setup.ps1`/`test.bat`) — the original version of this coursework project, fully superseded by `backend/`+`frontend/` above. It's kept in the repo for reference rather than deleted; see [`README_2.md`](README_2.md) for its own setup instructions and architecture notes. It is not part of the live application.

`academic-service/` and `academic-frontend/` are a separate, standalone experiment (their own build files) that was never integrated into the main system above.
